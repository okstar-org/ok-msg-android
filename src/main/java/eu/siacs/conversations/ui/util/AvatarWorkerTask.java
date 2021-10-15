package eu.siacs.conversations.ui.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import androidx.annotation.DimenRes;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.RejectedExecutionException;

import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;
import eu.siacs.conversations.entities.Account;
import eu.siacs.conversations.services.AvatarService;
import eu.siacs.conversations.services.XmppConnectionService;
import eu.siacs.conversations.ui.XmppActivity;

public class AvatarWorkerTask extends AsyncTask<AvatarService.Avatarable, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    private AvatarService.Avatarable avatarable = null;
    private @DimenRes
    int size;

    public AvatarWorkerTask(ImageView imageView, @DimenRes int size) {
        imageViewReference = new WeakReference<>(imageView);
        this.size = size;
    }

    @Override
    protected Bitmap doInBackground(AvatarService.Avatarable... params) {
        this.avatarable = params[0];
        final XmppActivity activity = XmppActivity.find(imageViewReference);
        if (activity == null) {
            return null;
        }
        return activity.avatarService().get(avatarable, (int) activity.getResources().getDimension(size), isCancelled());
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null && !isCancelled()) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
                imageView.setBackgroundColor(0x00000000);
            }
        }
    }

    public static boolean cancelPotentialWork(AvatarService.Avatarable avatarable, ImageView imageView) {
        final AvatarWorkerTask workerTask = getBitmapWorkerTask(imageView);

        if (workerTask != null) {
            final AvatarService.Avatarable old = workerTask.avatarable;
            if (old == null || avatarable != old) {
                workerTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    public static AvatarWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getAvatarWorkerTask();
            }
        }
        return null;
    }

    public static void loadAvatar(final AvatarService.Avatarable avatarable, final ImageView imageView, final @DimenRes int size) {
        loadAvatar(avatarable, imageView, size, false,null);
    }

    public static void loadAvatar(final String JidFromJabberNetwork, final AvatarService.Avatarable avatarable, final ImageView imageView, final @DimenRes int size) {
        loadAvatar(avatarable, imageView, size, false, JidFromJabberNetwork);
    }

    public static void loadAvatar(final AvatarService.Avatarable avatarable, final ImageView imageView, final @DimenRes int size, final boolean overlay) {
        loadAvatar(avatarable, imageView, size, overlay, null);
    }

    public static void loadAvatar(final AvatarService.Avatarable avatarable, final ImageView imageView, final @DimenRes int size, final boolean overlay, final String JidFromJabberNetwork) {
        if (cancelPotentialWork(avatarable, imageView)) {
            final XmppActivity activity = XmppActivity.find(imageView);
            if (activity == null) {
                return;
            }
            final Bitmap bm = activity.avatarService().get(avatarable, (int) activity.getResources().getDimension(size), false);
            setContentDescription(avatarable, imageView);
            if (bm != null && JidFromJabberNetwork == null) {
                cancelPotentialWork(avatarable, imageView);
                if (overlay) {
                    activity.xmppConnectionService.fileBackend.drawOverlay(bm, R.drawable.pencil_overlay, 0.35f, true);
                    imageView.setImageBitmap(bm);
                } else {
                    imageView.setImageBitmap(bm);
                }
                imageView.setBackgroundColor(0x00000000);
            } else if (JidFromJabberNetwork != null) {
                try {
                    new GetAvatarFromJabberNetwork(activity.xmppConnectionService, avatarable, imageView, size, overlay).execute(Config.CHANNEL_DISCOVERY + "/avatar/v1/" + JidFromJabberNetwork);
                } catch (Exception e) {
                    e.printStackTrace();
                    loadAvatar(avatarable, imageView, size, overlay, null);
                }
            } else {
                imageView.setBackgroundColor(avatarable.getAvatarBackgroundColor());
                imageView.setImageDrawable(null);
                final AvatarWorkerTask task = new AvatarWorkerTask(imageView, size);
                final AsyncDrawable asyncDrawable = new AsyncDrawable(activity.getResources(), null, task);
                if (overlay) {
                    activity.xmppConnectionService.fileBackend.drawOverlayFromDrawable(asyncDrawable, R.drawable.pencil_overlay, 1.0f);
                    imageView.setImageDrawable(asyncDrawable);
                } else {
                    imageView.setImageDrawable(asyncDrawable);
                }
                try {
                    task.execute(avatarable);
                } catch (final RejectedExecutionException ignored) {
                }
            }
        }
    }

    private static void setContentDescription(final AvatarService.Avatarable avatarable, final ImageView imageView) {
        final Context context = imageView.getContext();
        if (avatarable instanceof Account) {
            imageView.setContentDescription(context.getString(R.string.your_avatar));
        } else {
            imageView.setContentDescription(context.getString(R.string.avatar_for_x, avatarable.getAvatarName()));
        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<AvatarWorkerTask> avatarWorkerTaskReference;

        AsyncDrawable(Resources res, Bitmap bitmap, AvatarWorkerTask workerTask) {
            super(res, bitmap);
            avatarWorkerTaskReference = new WeakReference<>(workerTask);
        }

        AvatarWorkerTask getAvatarWorkerTask() {
            return avatarWorkerTaskReference.get();
        }
    }

    public static class GetAvatarFromJabberNetwork extends AsyncTask<String, Void, Bitmap> {
        Bitmap bitmap = null;
        AvatarService.Avatarable avatarable;
        ImageView imageView;
        int size;
        boolean overlay;
        XmppConnectionService xmppConnectionService;

        public GetAvatarFromJabberNetwork(final XmppConnectionService xmppConnectionService, final AvatarService.Avatarable avatarable, final ImageView imageView, final @DimenRes int size, final boolean overlay) {
            this.avatarable = avatarable;
            this.imageView = imageView;
            this.size = size;
            this.overlay = overlay;
            this.xmppConnectionService = xmppConnectionService;
        }

        @Override
        protected Bitmap doInBackground(String... url) {
            String stringUrl = url[0];
            try (InputStream inputStream = new java.net.URL(stringUrl).openStream()) {
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                //igrnore
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                loadAvatar(avatarable, imageView, size, overlay, null);
            }
        }
    }
}