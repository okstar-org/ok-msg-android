package eu.siacs.conversations.ui.adapter;

import org.okstar.okmsg.BobTransfer;

import eu.siacs.conversations.ui.widget.ClickableMovementMethod;
import eu.siacs.conversations.xml.Element;
import io.ipfs.cid.Cid;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static eu.siacs.conversations.entities.Message.DELETED_MESSAGE_BODY;
import static eu.siacs.conversations.entities.Message.DELETED_MESSAGE_BODY_OLD;
import static eu.siacs.conversations.persistance.FileBackend.formatTime;
import static eu.siacs.conversations.persistance.FileBackend.safeLongToInt;
import static eu.siacs.conversations.ui.SettingsActivity.PLAY_GIF_INSIDE;
import static eu.siacs.conversations.ui.SettingsActivity.SHOW_LINKS_INSIDE;
import static eu.siacs.conversations.ui.SettingsActivity.SHOW_MAPS_INSIDE;
import static eu.siacs.conversations.ui.util.MyLinkify.removeTrackingParameter;
import static eu.siacs.conversations.ui.util.MyLinkify.removeTrailingBracket;
import static eu.siacs.conversations.ui.util.MyLinkify.replaceYoutube;
import eu.siacs.conversations.ui.util.ShareUtil;
import org.okstar.okmsg.SwipeDetector;
import android.net.Uri;
import android.text.style.URLSpan;
import android.text.style.ImageSpan;
import android.text.style.ClickableSpan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import java.util.Map;
import java.util.HashMap;

import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.AsyncTask;
import android.widget.ListAdapter;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.common.base.Strings;
import com.squareup.picasso.Picasso;
import com.lelloman.identicon.view.GithubIdenticonView;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import org.okstar.okmsg.WebxdcUpdate;
import org.okstar.okmsg.WebxdcPage;

import eu.siacs.conversations.entities.Message;
import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;
import eu.siacs.conversations.crypto.axolotl.FingerprintStatus;
import eu.siacs.conversations.entities.Account;
import eu.siacs.conversations.entities.Conversation;
import eu.siacs.conversations.entities.Conversational;
import eu.siacs.conversations.entities.DownloadableFile;
import eu.siacs.conversations.entities.Message.FileParams;
import eu.siacs.conversations.entities.RtpSessionStatus;
import eu.siacs.conversations.entities.Transferable;
import eu.siacs.conversations.persistance.FileBackend;
import eu.siacs.conversations.services.AudioPlayer;
import eu.siacs.conversations.services.MessageArchiveService;
import eu.siacs.conversations.services.NotificationService;
import eu.siacs.conversations.ui.ConversationFragment;
import eu.siacs.conversations.ui.ConversationsActivity;
import eu.siacs.conversations.ui.XmppActivity;
import eu.siacs.conversations.ui.text.DividerSpan;
import eu.siacs.conversations.ui.text.QuoteSpan;
import eu.siacs.conversations.ui.util.AvatarWorkerTask;
import eu.siacs.conversations.ui.util.MyLinkify;
import eu.siacs.conversations.ui.util.QuoteHelper;
import eu.siacs.conversations.ui.util.StyledAttributes;
import eu.siacs.conversations.ui.util.ViewUtil;
import eu.siacs.conversations.ui.widget.RichLinkView;
import eu.siacs.conversations.utils.CryptoHelper;
import eu.siacs.conversations.utils.Emoticons;
import eu.siacs.conversations.utils.GeoHelper;
import eu.siacs.conversations.utils.MessageUtils;
import eu.siacs.conversations.utils.RichPreview;
import eu.siacs.conversations.utils.StylingHelper;
import eu.siacs.conversations.utils.ThemeHelper;
import eu.siacs.conversations.utils.TimeFrameUtils;
import eu.siacs.conversations.utils.UIHelper;
import eu.siacs.conversations.xmpp.Jid;
import eu.siacs.conversations.xmpp.mam.MamReference;
import me.drakeet.support.toast.ToastCompat;
import pl.droidsonroids.gif.GifImageView;

import android.widget.ListView;


public class MessageAdapter extends ArrayAdapter<Message> {

    public static final String DATE_SEPARATOR_BODY = "DATE_SEPARATOR";
    private static final int SENT = 0;
    private static final int RECEIVED = 1;
    private static final int STATUS = 2;
    private static final int DATE_SEPARATOR = 3;
    private static final int RTP_SESSION = 4;
    boolean isResendable = false;

    private final XmppActivity activity;
    private final AudioPlayer audioPlayer;
    private List<String> highlightedTerm = null;
    private final DisplayMetrics metrics;
    private OnContactPictureClicked mOnContactPictureClickedListener;
    private OnContactPictureClicked mOnMessageBoxClickedListener;
    private OnContactPictureClicked mOnMessageBoxSwipedListener;
    private OnContactPictureLongClicked mOnContactPictureLongClickedListener;
    private OnInlineImageLongClicked mOnInlineImageLongClickedListener;
    private boolean mIndicateReceived = false;
    private boolean mPlayGifInside = false;
    private boolean mShowLinksInside = false;
    private boolean mShowMapsInside = false;
    private final boolean mForceNames;
    private final Map<String, WebxdcUpdate> lastWebxdcUpdate = new HashMap<>();

    public MessageAdapter(final XmppActivity activity, final List<Message> messages, final boolean forceNames) {
        super(activity, 0, messages);
        this.activity = activity;
        this.audioPlayer = new AudioPlayer(this);
        metrics = getContext().getResources().getDisplayMetrics();
        updatePreferences();
        this.mForceNames = forceNames;
    }

    public MessageAdapter(final XmppActivity activity, final List<Message> messages) {
        this(activity, messages, false);
    }

    private static void resetClickListener(View... views) {
        for (View view : views) {
            if (view != null) view.setOnClickListener(null);
        }
    }

    public void flagDisableInputs() {
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void flagEnableInputs() {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void flagScreenOn() {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void flagScreenOff() {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public boolean autoPauseVoice() {
        return activity.xmppConnectionService.pauseVoiceOnMoveFromEar();
    }

    public void setVolumeControl(final int stream) {
        activity.setVolumeControlStream(stream);
    }

    public void setOnContactPictureClicked(OnContactPictureClicked listener) {
        this.mOnContactPictureClickedListener = listener;
    }

    public void setOnContactPictureLongClicked(
            OnContactPictureLongClicked listener) {
        this.mOnContactPictureLongClickedListener = listener;
    }

    public void setOnInlineImageLongClicked(OnInlineImageLongClicked listener) {
        this.mOnInlineImageLongClickedListener = listener;
    }

    public void setOnMessageBoxSwiped(OnContactPictureClicked listener) {
        this.mOnMessageBoxSwipedListener = listener;
    }

    public void setOnMessageBoxClicked(OnContactPictureClicked listener) {
        this.mOnMessageBoxClickedListener = listener;
    }

    public Activity getActivity() {
        return activity;
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }

    private int getItemViewType(Message message) {
        if (message.getType() == Message.TYPE_STATUS) {
            if (DATE_SEPARATOR_BODY.equals(message.getBody())) {
                return DATE_SEPARATOR;
            } else {
                return STATUS;
            }
        } else if (message.getType() == Message.TYPE_RTP_SESSION) {
            return RTP_SESSION;
        } else if (message.getStatus() <= Message.STATUS_RECEIVED) {
            return RECEIVED;
        } else {
            return SENT;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return this.getItemViewType(getItem(position));
    }

    private void displayStatus(ViewHolder viewHolder, final Message message, int type, boolean darkBackground) {
        long filesize = 0;
        String info = null;
        boolean error = false;
        viewHolder.user.setText(UIHelper.getDisplayedMucCounterpart(message.getCounterpart()));
        if (viewHolder.indicatorReceived != null) {
            viewHolder.indicatorReceived.setVisibility(View.GONE);
        }
        if (viewHolder.edit_indicator != null) {
            if (message.edited() && message.getModerated() == null) {
                viewHolder.edit_indicator.setVisibility(View.VISIBLE);
                viewHolder.edit_indicator.setImageResource(darkBackground ? R.drawable.ic_mode_edit_white_18dp : R.drawable.ic_mode_edit_black_18dp);
                viewHolder.edit_indicator.setAlpha(darkBackground ? 0.7f : 0.57f);
            } else {
                viewHolder.edit_indicator.setVisibility(View.GONE);
            }
        }
        if (viewHolder.retract_indicator != null) {
            if (message.getRetractId() != null) {
                viewHolder.retract_indicator.setVisibility(View.VISIBLE);
                viewHolder.retract_indicator.setImageResource(darkBackground ? R.drawable.ic_delete_white_18dp : R.drawable.ic_delete_black_18dp);
                viewHolder.retract_indicator.setAlpha(darkBackground ? 0.7f : 0.57f);
            } else {
                viewHolder.retract_indicator.setVisibility(View.GONE);
            }
        }
        final Transferable transferable = message.getTransferable();
        boolean multiReceived = message.getConversation().getMode() == Conversation.MODE_MULTI
                && message.getMergedStatus() <= Message.STATUS_RECEIVED;
        boolean singleReceived = message.getConversation().getMode() == Conversation.MODE_SINGLE
                && message.getMergedStatus() <= Message.STATUS_RECEIVED;
        if (message.isFileOrImage() || transferable != null || MessageUtils.unInitiatedButKnownSize(message)) {
            FileParams params = message.getFileParams();
            filesize = params.size;
            if (transferable != null && (transferable.getStatus() == Transferable.STATUS_FAILED || transferable.getStatus() == Transferable.STATUS_CANCELLED)) {
                error = true;
            }
        }
        switch (message.getMergedStatus()) {
            case Message.STATUS_WAITING:
                info = getContext().getString(R.string.waiting);
                break;
            case Message.STATUS_UNSEND:
                if (transferable != null) {
                    info = getContext().getString(R.string.sending);
                    showProgress(viewHolder, transferable, message);
                } else {
                    info = getContext().getString(R.string.sending);
                }
                break;
            case Message.STATUS_OFFERED:
                info = getContext().getString(R.string.offering);
                break;
            case Message.STATUS_SEND_RECEIVED:
                if (mIndicateReceived) {
                    if (viewHolder.indicatorReceived != null) {
                        viewHolder.indicatorReceived.setVisibility(View.VISIBLE);
                        viewHolder.indicatorReceived.setImageResource(darkBackground ? R.drawable.ic_check_white_18dp : R.drawable.ic_check_black_18dp);
                        viewHolder.indicatorReceived.setAlpha(darkBackground ? 0.7f : 0.57f);
                    }
                } else {
                    viewHolder.indicatorReceived.setVisibility(View.GONE);
                }
                break;
            case Message.STATUS_SEND_DISPLAYED:
                if (mIndicateReceived) {
                    if (viewHolder.indicatorReceived != null) {
                        viewHolder.indicatorReceived.setVisibility(View.VISIBLE);
                        viewHolder.indicatorReceived.setImageResource(darkBackground ? R.drawable.ic_check_all_white_18dp : R.drawable.ic_check_all_black_18dp);
                        viewHolder.indicatorReceived.setAlpha(darkBackground ? 0.7f : 0.57f);
                    }
                } else {
                    viewHolder.indicatorReceived.setVisibility(View.GONE);
                }
                break;
            case Message.STATUS_SEND_FAILED:
                DownloadableFile file = activity.xmppConnectionService.getFileBackend().getFile(message);
                if (isResendable && file.exists() || message.getResendCount() < activity.xmppConnectionService.maxResendTime()) {
                    info = getContext().getString(R.string.send_failed_resend);
                } else {
                    final String errorMessage = message.getErrorMessage();
                    if (Message.ERROR_MESSAGE_CANCELLED.equals(errorMessage)) {
                        info = getContext().getString(R.string.cancelled);
                    } else {
                        if (errorMessage != null) {
                            final String[] errorParts = errorMessage.split("\\u001f", 2);
                            if (errorParts.length == 2) {
                                switch (errorParts[0]) {
                                    case "file-too-large":
                                        info = getContext().getString(R.string.file_too_large);
                                        break;
                                    default:
                                        info = getContext().getString(R.string.send_failed);
                                        break;
                                }
                            } else {
                                info = getContext().getString(R.string.send_failed);
                            }
                        } else {
                            info = getContext().getString(R.string.send_failed);
                        }
                    }
                }
                error = true;
                break;
            default:
                if (mForceNames || multiReceived || (message.getTrueCounterpart() != null && message.getContact() != null)) {
                    final int shadowSize = 10;
                    showUsername(viewHolder, message, darkBackground);
                } else if (singleReceived) {
                    viewHolder.username.setVisibility(View.GONE);
                }
                break;
        }
        if (error && type == SENT) {
            if (darkBackground) {
                viewHolder.time.setTextAppearance(getContext(), R.style.TextAppearance_Conversations_Caption_Warning_OnDark);
            } else {
                viewHolder.time.setTextAppearance(getContext(), R.style.TextAppearance_Conversations_Caption_Warning);
            }
            DownloadableFile file = activity.xmppConnectionService.getFileBackend().getFile(message);
            if (file.exists() && message.getFileParams() != null) {
                if (activity.xmppConnectionService.mHttpConnectionManager.getAutoAcceptFileSize() >= message.getFileParams().size
                        && transferable != null
                        && transferable.getStatus() == Transferable.STATUS_FAILED) {
                    isResendable = true;
                    viewHolder.resend_button.setVisibility(View.GONE);
                } else {
                    isResendable = false;
                    viewHolder.resend_button.setVisibility(View.GONE);
                    /*
                    viewHolder.resend_button.setVisibility(View.VISIBLE);
                    viewHolder.resend_button.setText(R.string.send_again);
                    viewHolder.resend_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_resend_grey600_48dp, 0, 0, 0);
                    viewHolder.resend_button.setOnClickListener(v -> mConversationFragment.resendMessage(message));
                    */
                }
            } else {
                isResendable = false;
                viewHolder.resend_button.setVisibility(View.GONE);
            }
        } else {
            if (darkBackground) {
                viewHolder.time.setTextAppearance(getContext(), R.style.TextAppearance_Conversations_Caption_OnDark);
            } else {
                viewHolder.time.setTextAppearance(getContext(), R.style.TextAppearance_Conversations_Caption);
            }
            viewHolder.time.setTextColor(ThemeHelper.getMessageTextColor(getContext(), darkBackground, false));
        }
        if (!error && type == SENT) {
            viewHolder.resend_button.setVisibility(View.GONE);
        }
        if (message.getEncryption() == Message.ENCRYPTION_NONE) {
            viewHolder.indicator.setVisibility(View.GONE);
        } else {
            boolean verified = false;
            if (message.getEncryption() == Message.ENCRYPTION_AXOLOTL) {
                final FingerprintStatus status = message.getConversation().getAccount().getAxolotlService().getFingerprintTrust(message.getFingerprint());
                if (status != null && status.isVerified()) {
                    verified = true;
                }
            }
            if (verified) {
                viewHolder.indicator.setImageResource(darkBackground ? R.drawable.ic_verified_user_white_18dp : R.drawable.ic_verified_user_black_18dp);
            } else {
                viewHolder.indicator.setImageResource(darkBackground ? R.drawable.ic_lock_white_18dp : R.drawable.ic_lock_black_18dp);
            }
            if (darkBackground) {
                viewHolder.indicator.setAlpha(0.7f);
            } else {
                viewHolder.indicator.setAlpha(0.57f);
            }
            viewHolder.indicator.setVisibility(View.VISIBLE);
        }

        final String formattedTime = UIHelper.readableTimeDifferenceFull(getContext(), message.getMergedTimeSent());
        final String bodyLanguage = message.getBodyLanguage();
        final String bodyLanguageInfo = bodyLanguage == null ? "" : String.format(" \u00B7 %s", bodyLanguage.toUpperCase(Locale.US));
        if (message.getStatus() <= Message.STATUS_RECEIVED) {
            if ((filesize > 0) && (info != null)) {
                viewHolder.time.setText(formattedTime + " \u00B7 " + filesize + " \u00B7 " + info + bodyLanguageInfo);
            } else if ((filesize == 0) && (info != null)) {
                viewHolder.time.setText(formattedTime + " \u00B7 " + info + bodyLanguageInfo);
            } else if ((filesize > 0 ) && (info == null)) {
                viewHolder.time.setText(formattedTime + " \u00B7 " + filesize + bodyLanguageInfo);
            } else {
                viewHolder.time.setText(formattedTime + bodyLanguageInfo);
            }
        } else {
            if ((filesize > 0) && (info != null)) {
                viewHolder.time.setText(filesize + " \u00B7 " + info + bodyLanguageInfo);
            } else if ((filesize == 0) && (info != null)) {
                if (error) {
                    viewHolder.time.setText(info + " \u00B7 " + formattedTime + bodyLanguageInfo);
                } else {
                    viewHolder.time.setText(info);
                }
            } else if ((filesize > 0 ) && (info == null)) {
                viewHolder.time.setText(filesize + " \u00B7 " + formattedTime + bodyLanguageInfo);
            } else {
                viewHolder.time.setText(formattedTime + bodyLanguageInfo);
            }
        }
    }
    private void showUsername(ViewHolder viewHolder, Message message, boolean darkBackground) {
        if (message == null || viewHolder == null) {
            return;
        }
        viewHolder.username.setText(UIHelper.getColoredUsername(activity.xmppConnectionService, message));
        if (message.showUsername() || mForceNames) {
            viewHolder.username.setVisibility(View.VISIBLE);
        } else {
            viewHolder.username.setVisibility(View.GONE);
        }
        if (activity.xmppConnectionService.colored_muc_names() && ThemeHelper.showColoredUsernameBackGround(activity, darkBackground)) {
            viewHolder.username.setPadding(4, 2, 4, 2);
            viewHolder.username.setBackground(ContextCompat.getDrawable(activity, R.drawable.duration_background));
        } else {
            viewHolder.username.setPadding(4, 2, 4, 2);
            viewHolder.username.setBackground(null);
        }
    }

    private void displayInfoMessage(ViewHolder viewHolder, CharSequence text, boolean darkBackground, final Message message, int type) {
        displayDownloadableMessage(viewHolder, message, "", darkBackground, type);
        int imageVisibility = viewHolder.image.getVisibility();
        displayInfoMessage(viewHolder, text, darkBackground, message);
        viewHolder.image.setVisibility(imageVisibility);
    }

    private void displayInfoMessage(ViewHolder viewHolder, CharSequence text, boolean darkBackground, Message message) {
        viewHolder.download_button.setVisibility(View.GONE);
        viewHolder.audioPlayer.setVisibility(View.GONE);
        showImages(false, viewHolder);
        viewHolder.richlinkview.setVisibility(View.GONE);
        viewHolder.transfer.setVisibility(View.GONE);
        viewHolder.messageBody.setVisibility(View.VISIBLE);
        viewHolder.messageBody.setText(text);
        showProgress(viewHolder, message.getTransferable(), message);
        if (darkBackground) {
            viewHolder.messageBody.setTextAppearance(getContext(), R.style.TextAppearance_Conversations_Body1_Secondary_OnDark);
        } else {
            viewHolder.messageBody.setTextAppearance(getContext(), R.style.TextAppearance_Conversations_Body1_Secondary);
        }
        viewHolder.messageBody.setTextIsSelectable(false);
    }

    private void showProgress(final ViewHolder viewHolder, final Transferable transferable, final Message message) {
        if (transferable != null) {
            if (message.fileIsTransferring()) {
                viewHolder.transfer.setVisibility(View.VISIBLE);
                viewHolder.progressBar.setProgress(transferable.getProgress());
                Drawable icon = activity.getResources().getDrawable(R.drawable.ic_cancel_black_24dp);
                Drawable drawable = DrawableCompat.wrap(icon);
                DrawableCompat.setTint(drawable, StyledAttributes.getColor(getContext(), R.attr.colorAccent));
                viewHolder.cancel_transfer.setImageDrawable(drawable);
                viewHolder.cancel_transfer.setEnabled(true);
                viewHolder.cancel_transfer.setOnClickListener(v -> {
                    try {
                        if (activity instanceof ConversationsActivity) {
                            ConversationFragment conversationFragment = ConversationFragment.get(activity);
                            if (conversationFragment != null) {
                                activity.invalidateOptionsMenu();
                                conversationFragment.cancelTransmission(message);
                            }
                        }
                    } catch (Exception e) {
                        viewHolder.cancel_transfer.setEnabled(false);
                        e.printStackTrace();
                    }
                });
            } else {
                viewHolder.transfer.setVisibility(View.GONE);
            }
        } else {
            viewHolder.transfer.setVisibility(View.GONE);
        }
    }

    private void displayEmojiMessage(final ViewHolder viewHolder, final String body, final boolean darkBackground) {
        viewHolder.download_button.setVisibility(View.GONE);
        viewHolder.audioPlayer.setVisibility(View.GONE);
        showImages(false, viewHolder);
        viewHolder.richlinkview.setVisibility(View.GONE);
        viewHolder.transfer.setVisibility(View.GONE);
        viewHolder.messageBody.setVisibility(View.VISIBLE);
        if (darkBackground) {
            viewHolder.messageBody.setTextAppearance(getContext(), R.style.TextAppearance_Conversations_Body1_Emoji_OnDark);
        } else {
            viewHolder.messageBody.setTextAppearance(getContext(), R.style.TextAppearance_Conversations_Body1_Emoji);
        }
        final Spannable span = new SpannableString(body);
        final float size = Emoticons.isEmoji(body) ? 3.0f : 2.0f;
        span.setSpan(new RelativeSizeSpan(size), 0, body.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        viewHolder.messageBody.setText(span);
    }

    private void displayXmppMessage(final ViewHolder viewHolder, final String body) {
        String contact = body.toLowerCase();
        contact = contact.split(":")[1];
        boolean group;
        try {
            group = ((contact.split("\\?")[1]) != null && (contact.split("\\?")[1]).length() > 0 && (contact.split("\\?")[1]).equalsIgnoreCase("join"));
        } catch (Exception e) {
            group = false;
        }
        contact = contact.split("\\?")[0];
        final String add_contact = activity.getString(R.string.add_to_contact_list) + " (" + contact + ")";
        viewHolder.audioPlayer.setVisibility(View.GONE);
        viewHolder.download_button.setVisibility(View.VISIBLE);
        viewHolder.download_button.setText(add_contact);
        if (group) {
            final Drawable icon = activity.getResources().getDrawable(R.drawable.ic_account_multiple_plus_grey600_48dp);
            final Drawable drawable = DrawableCompat.wrap(icon);
            DrawableCompat.setTint(drawable, StyledAttributes.getColor(getContext(), R.attr.colorAccent));
            viewHolder.download_button.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        } else {
            final Drawable icon = activity.getResources().getDrawable(R.drawable.ic_account_plus_grey600_48dp);
            final Drawable drawable = DrawableCompat.wrap(icon);
            DrawableCompat.setTint(drawable, StyledAttributes.getColor(getContext(), R.attr.colorAccent));
            viewHolder.download_button.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }
        viewHolder.download_button.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(body));
                activity.startActivity(intent);
                activity.overridePendingTransition(R.animator.fade_in, R.animator.fade_out);
            } catch (Exception e) {
                ToastCompat.makeText(activity, R.string.no_application_found_to_view_contact, ToastCompat.LENGTH_LONG).show();
            }

        });
        showImages(false, viewHolder);
        viewHolder.richlinkview.setVisibility(View.GONE);
        viewHolder.transfer.setVisibility(View.GONE);
        viewHolder.messageBody.setVisibility(View.GONE);
    }

    private void applyQuoteSpan(SpannableStringBuilder body, int start, int end, boolean darkBackground) {
        if (start > 1 && !"\n\n".equals(body.subSequence(start - 2, start).toString())) {
            body.insert(start++, "\n");
            body.setSpan(
                    new DividerSpan(false),
                    start - ("\n".equals(body.subSequence(start - 2, start - 1).toString()) ? 2 : 1),
                    start,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            end++;
        }
        if (end < body.length() - 1 && !"\n\n".equals(body.subSequence(end, end + 2).toString())) {
            body.insert(end, "\n");
            body.setSpan(
                    new DividerSpan(false),
                    end,
                    end + ("\n".equals(body.subSequence(end + 1, end + 2).toString()) ? 2 : 1),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        int color = ThemeHelper.messageTextColor(activity);
        final DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        body.setSpan(new QuoteSpan(color, metrics), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * Applies QuoteSpan to group of lines which starts with > or » characters.
     * Appends likebreaks and applies DividerSpan to them to show a padding between quote and text.
     */
    public boolean handleTextQuotes(SpannableStringBuilder body, boolean darkBackground) {
        boolean startsWithQuote = false;
        int quoteDepth = 1;
        while (QuoteHelper.bodyContainsQuoteStart(body) && quoteDepth <= Config.QUOTE_MAX_DEPTH) {
            char previous = '\n';
            int lineStart = -1;
            int lineTextStart = -1;
            int quoteStart = -1;
            for (int i = 0; i <= body.length(); i++) {
                char current = body.length() > i ? body.charAt(i) : '\n';
                if (lineStart == -1) {
                    if (previous == '\n') {
                        if (i < body.length() && QuoteHelper.isPositionQuoteStart(body, i)) {
                            // Line start with quote
                            lineStart = i;
                            if (quoteStart == -1) quoteStart = i;
                            if (i == 0) startsWithQuote = true;
                        } else if (quoteStart >= 0) {
                            // Line start without quote, apply spans there
                            applyQuoteSpan(body, quoteStart, i - 1, darkBackground);
                            quoteStart = -1;
                        }
                    }
                } else {
                    // Remove extra spaces between > and first character in the line
                    // > character will be removed too
                    if (current != ' ' && lineTextStart == -1) {
                        lineTextStart = i;
                    }
                    if (current == '\n') {
                        body.delete(lineStart, lineTextStart);
                        i -= lineTextStart - lineStart;
                        if (i == lineStart) {
                            // Avoid empty lines because span over empty line can be hidden
                            body.insert(i++, " ");
                        }
                        lineStart = -1;
                        lineTextStart = -1;
                    }
                }
                previous = current;
            }
            if (quoteStart >= 0) {
                // Apply spans to finishing open quote
                applyQuoteSpan(body, quoteStart, body.length(), darkBackground);
            }
            quoteDepth++;
        }
        return startsWithQuote;
    }

    private void displayTextMessage(final ViewHolder viewHolder, final Message message, boolean darkBackground, int type) {
        viewHolder.download_button.setVisibility(View.GONE);
        showImages(false, viewHolder);
        viewHolder.richlinkview.setVisibility(View.GONE);
        viewHolder.transfer.setVisibility(View.GONE);
        viewHolder.audioPlayer.setVisibility(View.GONE);
        viewHolder.messageBody.setVisibility(View.GONE);
        if (darkBackground) {
            viewHolder.messageBody.setTextAppearance(getContext(), R.style.TextAppearance_Conversations_Body1_OnDark);
        } else {
            viewHolder.messageBody.setTextAppearance(getContext(), R.style.TextAppearance_Conversations_Body1);
        }
        viewHolder.messageBody.setHighlightColor(darkBackground ? type == SENT ? StyledAttributes.getColor(activity, R.attr.colorAccent) : StyledAttributes.getColor(activity, R.attr.colorAccent) : StyledAttributes.getColor(activity, R.attr.colorAccent));
        viewHolder.messageBody.setTypeface(null, Typeface.NORMAL);
        if (message.getBody() != null && !message.getBody().equals("")) {
            viewHolder.messageBody.setVisibility(View.VISIBLE);
            final SpannableString nick = UIHelper.getColoredUsername(activity.xmppConnectionService, message);
            Drawable fallbackImg = ResourcesCompat.getDrawable(activity.getResources(), activity.getThemeResource(R.attr.ic_attach_photo, R.drawable.ic_attach_photo), null);
            fallbackImg.setBounds(FileBackend.rectForSize(fallbackImg.getIntrinsicWidth(), fallbackImg.getIntrinsicHeight(), (int) (metrics.density * 32)));
            SpannableStringBuilder body =  new SpannableStringBuilder(replaceYoutube(activity.getApplicationContext(), message.getMergedBody((cid) -> {
                try {
                    DownloadableFile f = activity.xmppConnectionService.getFileForCid(cid);
                    if (f == null || !f.canRead()) {
                        if (!message.trusted() && !message.getConversation().canInferPresence()) return null;

                        try {
                            new BobTransfer(BobTransfer.uri(cid), message.getConversation().getAccount(), message.getCounterpart(), activity.xmppConnectionService).start();
                        } catch (final NoSuchAlgorithmException | URISyntaxException e) { }
                        return null;
                    }
                    Drawable d = activity.xmppConnectionService.getFileBackend().getThumbnail(f, activity.getResources(), (int) (metrics.density * 288), true);
                    if (d == null) {
                        new ThumbnailTask().execute(f);
                    } else {
                        d = d.getConstantState().newDrawable();
                        d.setBounds(FileBackend.rectForSize(d.getIntrinsicWidth(), d.getIntrinsicHeight(), (int) (metrics.density * 32)));
                    }
                    return d;
                } catch (final IOException e) {
                    return fallbackImg;
                }
            }, fallbackImg)));
            if (message.getBody().equals(DELETED_MESSAGE_BODY)) {
                body = body.replace(0, DELETED_MESSAGE_BODY.length(), activity.getString(R.string.message_deleted));
            } else if (message.getBody().equals(DELETED_MESSAGE_BODY_OLD)) {
                body = body.replace(0, DELETED_MESSAGE_BODY_OLD.length(), activity.getString(R.string.message_deleted));
            } else {
                boolean hasMeCommand = message.hasMeCommand();
                if (hasMeCommand) {
                    body = body.replace(0, Message.ME_COMMAND.length(), nick);
                }
                if (body.length() > Config.MAX_DISPLAY_MESSAGE_CHARS) {
                    body = new SpannableStringBuilder(body, 0, Config.MAX_DISPLAY_MESSAGE_CHARS);
                    body.append("\u2026");
                }
                final Message.MergeSeparator[] mergeSeparators = body.getSpans(0, body.length(), Message.MergeSeparator.class);
                for (Message.MergeSeparator mergeSeparator : mergeSeparators) {
                    int start = body.getSpanStart(mergeSeparator);
                    int end = body.getSpanEnd(mergeSeparator);
                    body.setSpan(new DividerSpan(true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                for (final android.text.style.QuoteSpan quote : body.getSpans(0, body.length(), android.text.style.QuoteSpan.class)) {
                    int start = body.getSpanStart(quote);
                    int end = body.getSpanEnd(quote);
                    body.removeSpan(quote);
                    applyQuoteSpan(body, start, end, darkBackground);
                }
                final boolean startsWithQuote = handleTextQuotes(body, darkBackground);
                if (!message.isPrivateMessage()) {
                    if (hasMeCommand) {
                        body.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, nick.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } else {
                    String privateMarker;
                    if (message.getStatus() <= Message.STATUS_RECEIVED) {
                        privateMarker = activity.getString(R.string.private_message);
                    } else {
                        Jid cp = message.getCounterpart();
                        privateMarker = activity.getString(R.string.private_message_to, Strings.nullToEmpty(cp == null ? null : cp.getResource()));
                    }
                    body.insert(0, privateMarker);
                    final int privateMarkerIndex = privateMarker.length();
                    if (startsWithQuote) {
                        body.insert(privateMarkerIndex, "\n\n");
                        body.setSpan(new DividerSpan(false), privateMarkerIndex, privateMarkerIndex + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        body.insert(privateMarkerIndex, " ");
                    }
                    body.setSpan(new ForegroundColorSpan(ThemeHelper.getMessageTextColorPrivate(activity)), 0, privateMarkerIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    body.setSpan(new StyleSpan(Typeface.BOLD), 0, privateMarkerIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    if (hasMeCommand) {
                        body.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), privateMarkerIndex + 1, privateMarkerIndex + 1 + nick.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                if (message.getConversation().getMode() == Conversation.MODE_MULTI && message.getStatus() == Message.STATUS_RECEIVED) {
                    if (message.getConversation() instanceof Conversation) {
                        final Conversation conversation = (Conversation) message.getConversation();
                        Pattern pattern = NotificationService.generateNickHighlightPattern(conversation.getMucOptions().getActualNick());
                        Matcher matcher = pattern.matcher(body);
                        while (matcher.find()) {
                            body.setSpan(new StyleSpan(Typeface.BOLD), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                        pattern = NotificationService.generateNickHighlightPattern(conversation.getMucOptions().getActualName());
                        matcher = pattern.matcher(body);
                        while (matcher.find()) {
                            body.setSpan(new StyleSpan(Typeface.BOLD), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }
                final Matcher matcher = Emoticons.getEmojiPattern(body).matcher(body);
                while (matcher.find()) {
                    if (matcher.start() < matcher.end()) {
                        body.setSpan(new RelativeSizeSpan(1.5f), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                StylingHelper.format(body, viewHolder.messageBody.getCurrentTextColor(), true);
                if (highlightedTerm != null) {
                    StylingHelper.highlight(activity, body, highlightedTerm, StylingHelper.isDarkText(viewHolder.messageBody));
                }
            }
            if (message.isWebUri() || message.getWebUri() != null) {
                displayRichLinkMessage(viewHolder, message, darkBackground);
            }
            MyLinkify.addLinks(body, message.getConversation().getAccount(), message.getConversation().getJid());
            viewHolder.messageBody.setText(body);
            viewHolder.messageBody.setAutoLinkMask(0);
            BetterLinkMovementMethod method = new BetterLinkMovementMethod() {
                @Override
                protected void dispatchUrlLongClick(TextView tv, ClickableSpan span) {
                    if (span instanceof URLSpan || mOnInlineImageLongClickedListener == null) {
                        tv.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0f, 0f, 0));
                        super.dispatchUrlLongClick(tv, span);
                        return;
                    }

                    Spannable body = (Spannable) tv.getText();
                    ImageSpan[] imageSpans = body.getSpans(body.getSpanStart(span), body.getSpanEnd(span), ImageSpan.class);
                    if (imageSpans.length > 0) {
                        Uri uri = Uri.parse(imageSpans[0].getSource());
                        Cid cid = BobTransfer.cid(uri);
                        if (cid == null) return;
                        if (mOnInlineImageLongClickedListener.onInlineImageLongClicked(cid)) {
                            tv.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0f, 0f, 0));
                        }
                    }
                }
            };            method.setOnLinkLongClickListener((tv, url) -> {
                tv.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0f, 0f, 0));
                ShareUtil.copyLinkToClipboard(activity, url);
                return true;
            });
            viewHolder.messageBody.setMovementMethod(method);
        } else {
            viewHolder.messageBody.setText("");
            viewHolder.messageBody.setTextIsSelectable(false);
        }
    }

    private void displayDownloadableMessage(ViewHolder viewHolder, final Message message, String text, final boolean darkBackground, final int type) {
        displayTextMessage(viewHolder, message, darkBackground, type);
        viewHolder.image.setVisibility(View.GONE);
        List<Element> thumbs = message.getFileParams() != null ? message.getFileParams().getThumbnails() : null;
        if (thumbs != null && !thumbs.isEmpty()) {
            for (Element thumb : thumbs) {
                Uri uri = Uri.parse(thumb.getAttribute("uri"));
                if (uri.getScheme().equals("data")) {
                    String[] parts = uri.getSchemeSpecificPart().split(",", 2);
                    parts = parts[0].split(";");
                    if (!parts[0].equals("image/blurhash") && !parts[0].equals("image/thumbhash") && !parts[0].equals("image/jpeg") && !parts[0].equals("image/png") && !parts[0].equals("image/webp") && !parts[0].equals("image/gif")) continue;
                } else if (uri.getScheme().equals("cid")) {
                    Cid cid = BobTransfer.cid(uri);
                    if (cid == null) continue;
                    DownloadableFile f = activity.xmppConnectionService.getFileForCid(cid);
                    if (f == null || !f.canRead()) {
                        if (!message.trusted() && !message.getConversation().canInferPresence()) continue;

                        try {
                            new BobTransfer(BobTransfer.uri(cid), message.getConversation().getAccount(), message.getCounterpart(), activity.xmppConnectionService).start();
                        } catch (final NoSuchAlgorithmException | URISyntaxException e) { }
                        continue;
                    }
                } else {
                    continue;
                }

                int width = message.getFileParams().width;
                if (width < 1 && thumb.getAttribute("width") != null) width = Integer.parseInt(thumb.getAttribute("width"));
                if (width < 1) width = 1920;

                int height = message.getFileParams().height;
                if (height < 1 && thumb.getAttribute("height") != null) height = Integer.parseInt(thumb.getAttribute("height"));
                if (height < 1) height = 1080;

                viewHolder.image.setVisibility(View.VISIBLE);
                imagePreviewLayout(width, height, viewHolder.image);
                activity.loadBitmap(message, viewHolder.image);
                viewHolder.image.setOnClickListener(v -> ConversationFragment.downloadFile(activity, message));

                break;
            }
        }
        viewHolder.audioPlayer.setVisibility(View.GONE);
        showImages(false, viewHolder);
        viewHolder.richlinkview.setVisibility(View.GONE);
        viewHolder.transfer.setVisibility(View.GONE);
        viewHolder.download_button.setVisibility(View.VISIBLE);
        viewHolder.download_button.setText(text);
        final Drawable icon = activity.getResources().getDrawable(R.drawable.ic_download_grey600_48dp);
        final Drawable drawable = DrawableCompat.wrap(icon);
        DrawableCompat.setTint(drawable, StyledAttributes.getColor(getContext(), R.attr.colorAccent));
        viewHolder.download_button.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        viewHolder.download_button.setOnClickListener(v -> ConversationFragment.downloadFile(activity, message));
    }


    private void displayWebxdcMessage(ViewHolder viewHolder, final Message message, final boolean darkBackground, final int type) {
        Cid webxdcCid = message.getFileParams().getCids().get(0);
        WebxdcPage webxdc = new WebxdcPage(webxdcCid, message, activity.xmppConnectionService);
        displayTextMessage(viewHolder, message, darkBackground, type);
        viewHolder.image.setVisibility(View.GONE);
        viewHolder.audioPlayer.setVisibility(View.GONE);
        viewHolder.download_button.setVisibility(View.VISIBLE);
        viewHolder.download_button.setText("Open " + webxdc.getName());
        viewHolder.download_button.setOnClickListener(v -> {
            Conversation conversation = (Conversation) message.getConversation();
            if (!conversation.switchToSession("webxdc\0" + message.getUuid())) {
                conversation.startWebxdc(webxdc);
            }
        });

        final WebxdcUpdate lastUpdate;
        synchronized(lastWebxdcUpdate) { lastUpdate = lastWebxdcUpdate.get(message.getUuid()); }
        if (lastUpdate == null) {
            new Thread(() -> {
                final WebxdcUpdate update = activity.xmppConnectionService.findLastWebxdcUpdate(message);
                if (update != null) {
                    synchronized(lastWebxdcUpdate) { lastWebxdcUpdate.put(message.getUuid(), update); }
                    activity.xmppConnectionService.updateConversationUi();
                }
            }).start();
        } else {
            if (lastUpdate != null && (lastUpdate.getSummary() != null || lastUpdate.getDocument() != null)) {
                viewHolder.messageBody.setVisibility(View.VISIBLE);
                viewHolder.messageBody.setText(
                        (lastUpdate.getDocument() == null ? "" : lastUpdate.getDocument() + "\n") +
                                (lastUpdate.getSummary() == null ? "" : lastUpdate.getSummary())
                );
            }
        }

        final LruCache<String, Drawable> cache = activity.xmppConnectionService.getDrawableCache();
        final Drawable d = cache.get("webxdc:icon:" + webxdcCid);
        if (d == null) {
            new Thread(() -> {
                Drawable icon = webxdc.getIcon();
                if (icon != null) {
                    cache.put("webxdc:icon:" + webxdcCid, icon);
                    activity.xmppConnectionService.updateConversationUi();
                }
            }).start();
        } else {
            viewHolder.image.setVisibility(View.VISIBLE);
            viewHolder.image.setImageDrawable(d);
        }
    }

    private void displayOpenableMessage(ViewHolder viewHolder, final Message message, final boolean darkBackground, final int type) {
        displayTextMessage(viewHolder, message, darkBackground, type);
        viewHolder.download_button.setVisibility(View.VISIBLE);
        viewHolder.audioPlayer.setVisibility(View.GONE);
        showImages(false, viewHolder);
        viewHolder.richlinkview.setVisibility(View.GONE);
        viewHolder.transfer.setVisibility(View.GONE);
        final String mimeType = message.getMimeType();
        if (mimeType != null && message.getMimeType().contains("vcard")) {
            try {
                showVCard(message, viewHolder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (mimeType != null && message.getMimeType().contains("calendar")) {
            final Drawable icon = activity.getResources().getDrawable(R.drawable.ic_calendar_grey600_48dp);
            final Drawable drawable = DrawableCompat.wrap(icon);
            DrawableCompat.setTint(drawable, StyledAttributes.getColor(getContext(), R.attr.colorAccent));
            viewHolder.download_button.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            viewHolder.download_button.setText(activity.getString(R.string.open_x_file, UIHelper.getFileDescriptionString(activity, message)));
        } else if (mimeType != null && message.getMimeType().equals("application/vnd.android.package-archive")) {
            try {
                showAPK(message, viewHolder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (mimeType != null && message.getMimeType().contains("video")) {
            final Drawable icon = activity.getResources().getDrawable(R.drawable.ic_video_grey600_48dp);
            final Drawable drawable = DrawableCompat.wrap(icon);
            DrawableCompat.setTint(drawable, StyledAttributes.getColor(getContext(), R.attr.colorAccent));
            viewHolder.download_button.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            viewHolder.download_button.setText(activity.getString(R.string.open_x_file, UIHelper.getFileDescriptionString(activity, message)));
        } else if (mimeType != null && message.getMimeType().contains("image")) {
            final Drawable icon = activity.getResources().getDrawable(R.drawable.ic_image_grey600_48dp);
            final Drawable drawable = DrawableCompat.wrap(icon);
            DrawableCompat.setTint(drawable, StyledAttributes.getColor(getContext(), R.attr.colorAccent));
            viewHolder.download_button.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            viewHolder.download_button.setText(activity.getString(R.string.open_x_file, UIHelper.getFileDescriptionString(activity, message)));
        } else if (mimeType != null && message.getMimeType().contains("audio")) {
            final Drawable icon = activity.getResources().getDrawable(R.drawable.ic_audio_grey600_48dp);
            final Drawable drawable = DrawableCompat.wrap(icon);
            DrawableCompat.setTint(drawable, StyledAttributes.getColor(getContext(), R.attr.colorAccent));
            viewHolder.download_button.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            viewHolder.download_button.setText(activity.getString(R.string.open_x_file, UIHelper.getFileDescriptionString(activity, message)));
        } else {
            final Drawable icon = activity.getResources().getDrawable(R.drawable.ic_file_grey600_48dp);
            final  Drawable drawable = DrawableCompat.wrap(icon);
            DrawableCompat.setTint(drawable, StyledAttributes.getColor(getContext(), R.attr.colorAccent));
            viewHolder.download_button.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            viewHolder.download_button.setText(activity.getString(R.string.open_x_file, UIHelper.getFileDescriptionString(activity, message)));
        }
        viewHolder.download_button.setOnClickListener(v -> openDownloadable(message));
    }

    private void showAPK(final Message message, final ViewHolder viewHolder) {
        String APKName = "";
        if (message.getFileParams().subject.length() != 0) {
            try {
                byte[] data = Base64.decode(message.getFileParams().subject, Base64.DEFAULT);
                APKName = new String(data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                APKName = "";
                e.printStackTrace();
            }
        }
        final Drawable icon = activity.getResources().getDrawable(R.drawable.ic_android_grey600_48dp);
        final Drawable drawable = DrawableCompat.wrap(icon);
        DrawableCompat.setTint(drawable, StyledAttributes.getColor(getContext(), R.attr.colorAccent));
        viewHolder.download_button.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        viewHolder.download_button.setText(activity.getString(R.string.open_x_file, UIHelper.getFileDescriptionString(activity, message) + APKName));
    }

    private void showVCard(final Message message, ViewHolder viewHolder) {
        String VCardName = "";
        if (message.getFileParams().subject.length() != 0) {
            try {
                byte[] data = Base64.decode(message.getFileParams().subject, Base64.DEFAULT);
                VCardName = new String(data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                VCardName = "";
                e.printStackTrace();
            }
        }
        final  Drawable icon = activity.getResources().getDrawable(R.drawable.ic_account_card_details_grey600_48dp);
        final  Drawable drawable = DrawableCompat.wrap(icon);
        DrawableCompat.setTint(drawable, StyledAttributes.getColor(getContext(), R.attr.colorAccent));
        viewHolder.download_button.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        viewHolder.download_button.setText(activity.getString(R.string.open_x_file, UIHelper.getFileDescriptionString(activity, message) + VCardName));
    }

    private void displayRichLinkMessage(final ViewHolder viewHolder, final Message message, boolean darkBackground) {
        toggleWhisperInfo(viewHolder, message, true, darkBackground);
        viewHolder.audioPlayer.setVisibility(View.GONE);
        showImages(false, viewHolder);
        viewHolder.download_button.setVisibility(View.GONE);
        viewHolder.transfer.setVisibility(View.GONE);
        String url;
        if (message.isWebUri()) {
            url = removeTrackingParameter(Uri.parse(message.getBody().trim())).toString();
        } else {
            url = removeTrackingParameter(Uri.parse(message.getWebUri())).toString();
        }
        final String link = replaceYoutube(activity.getApplicationContext(), url);
        Log.d(Config.LOGTAG, "Weburi body for preview: " + link);
        final boolean dataSaverDisabled = activity.xmppConnectionService.isDataSaverDisabled();
        viewHolder.richlinkview.setVisibility(mShowLinksInside ? View.VISIBLE : View.GONE);
        if (mShowLinksInside) {
            final int color = ThemeHelper.messageTextColor(activity);
            final float target = activity.getResources().getDimension(R.dimen.image_preview_width);
            final int scaledH;
            if (Math.max(100, 100) * metrics.density <= target) {
                scaledH = (int) (100 * metrics.density);
            } else if (Math.max(100, 100) <= target) {
                scaledH = 100;
            } else {
                scaledH = (int) (100 / ((double) 100 / target));
            }
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            layoutParams.setMargins(0, (int) (metrics.density * 4), 0, (int) (metrics.density * 4));
            viewHolder.richlinkview.setLayoutParams(layoutParams);
            viewHolder.richlinkview.setMinimumHeight(scaledH);
            final String weburl;
            if (link.startsWith("http://") || link.startsWith("https://")) {
                weburl = removeTrailingBracket(link);
            } else {
                weburl = "http://" + removeTrailingBracket(link);
            }
            Log.d(Config.LOGTAG, "Weburi for preview: " + weburl);
            viewHolder.richlinkview.setLink(weburl, message.getUuid(), dataSaverDisabled, activity.xmppConnectionService, color, new RichPreview.ViewListener() {

                @Override
                public void onSuccess(boolean status) {
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                    viewHolder.richlinkview.setVisibility(View.GONE);
                }
            });
        }
    }

    private void displayLocationMessage(ViewHolder viewHolder, final Message message, final boolean darkBackground, final int type) {
        displayTextMessage(viewHolder, message, darkBackground, type);
        viewHolder.audioPlayer.setVisibility(View.GONE);
        final String url = GeoHelper.MapPreviewUri(message, activity);
        showImages(false, viewHolder);
        viewHolder.richlinkview.setVisibility(View.GONE);
        viewHolder.transfer.setVisibility(View.GONE);
        if (mShowMapsInside) {
            showImages(mShowMapsInside, 0, false, viewHolder);
            final double target = activity.getResources().getDimension(R.dimen.image_preview_width);
            final int scaledW;
            final int scaledH;
            if (Math.max(500, 500) * metrics.density <= target) {
                scaledW = (int) (500 * metrics.density);
                scaledH = (int) (500 * metrics.density);
            } else if (Math.max(500, 500) <= target) {
                scaledW = 500;
                scaledH = 500;
            } else {
                scaledW = (int) target;
                scaledH = (int) (500 / ((double) 500 / target));
            }
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(scaledW, scaledH);
            layoutParams.setMargins(0, (int) (metrics.density * 4), 0, (int) (metrics.density * 4));
            viewHolder.images.setLayoutParams(layoutParams);
            viewHolder.image.setOnClickListener(v -> showLocation(message));
            Picasso .get()
                    .load(Uri.parse(url))
                    .placeholder(R.drawable.ic_map_marker_grey600_48dp)
                    .error(R.drawable.ic_map_marker_grey600_48dp)
                    .into(viewHolder.image);
            viewHolder.image.setMaxWidth(500);
            viewHolder.image.setAdjustViewBounds(true);
            viewHolder.download_button.setVisibility(View.GONE);
        } else {
            showImages(false, viewHolder);
            viewHolder.download_button.setVisibility(View.VISIBLE);
            viewHolder.download_button.setText(R.string.show_location);
            final Drawable icon = activity.getResources().getDrawable(R.drawable.ic_map_marker_grey600_48dp);
            final Drawable drawable = DrawableCompat.wrap(icon);
            DrawableCompat.setTint(drawable, StyledAttributes.getColor(getContext(), R.attr.colorAccent));
            viewHolder.download_button.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            viewHolder.download_button.setOnClickListener(v -> showLocation(message));
        }
    }

    private void displayAudioMessage(ViewHolder viewHolder, Message message, boolean darkBackground, final int type) {
        final Resources res = activity.getResources();
        viewHolder.messageBody.setWidth((int) res.getDimension(R.dimen.audio_player_width));
        displayTextMessage(viewHolder, message, darkBackground, type);
        showImages(false, viewHolder);
        viewHolder.richlinkview.setVisibility(View.GONE);
        viewHolder.transfer.setVisibility(View.GONE);
        viewHolder.download_button.setVisibility(View.GONE);
        final RelativeLayout audioPlayer = viewHolder.audioPlayer;
        audioPlayer.setVisibility(View.VISIBLE);
        AudioPlayer.ViewHolder.get(audioPlayer).setTheme(darkBackground);
        this.audioPlayer.init(audioPlayer, message);
    }

    private boolean showTitle(Message message) {
        boolean show = false;
        if (message.getFileParams().subject.length() != 0) {
            try {
                byte[] data = Base64.decode(message.getFileParams().subject, Base64.DEFAULT);
                show = (new String(data, "UTF-8").length() != 0);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return show;
    }

    private String getTitle(Message message) {
        if (message.getFileParams().subject.length() != 0) {
            try {
                byte[] data = Base64.decode(message.getFileParams().subject, Base64.DEFAULT);
                return new String(data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private void displayMediaPreviewMessage(ViewHolder viewHolder, final Message message, final boolean darkBackground, final int type) {
        displayTextMessage(viewHolder, message, darkBackground, type);
        viewHolder.download_button.setVisibility(View.GONE);
        viewHolder.audioPlayer.setVisibility(View.GONE);
        viewHolder.richlinkview.setVisibility(View.GONE);
        viewHolder.transfer.setVisibility(View.GONE);
        final DownloadableFile file = activity.xmppConnectionService.getFileBackend().getFile(message);
        if (file != null && !file.exists() && !message.isFileDeleted()) {
            new Thread(new markFileDeletedFinisher(message, activity)).start();
            displayInfoMessage(viewHolder, activity.getString(R.string.file_deleted), darkBackground, message);
            ToastCompat.makeText(activity, R.string.file_deleted, ToastCompat.LENGTH_SHORT).show();
            return;
        }
        final String mime = file.getMimeType();
        final boolean isGif = mime != null && mime.equals("image/gif");
        final int mediaRuntime = message.getFileParams().runtime;
        if (isGif && mPlayGifInside) {
            showImages(true, mediaRuntime, true, viewHolder);
            Log.d(Config.LOGTAG, "Gif Image file");
            final FileParams params = message.getFileParams();
            final float target = activity.getResources().getDimension(R.dimen.image_preview_width);
            final int scaledW;
            final int scaledH;
            if (Math.max(params.height, params.width) * metrics.density <= target) {
                scaledW = (int) (params.width * metrics.density);
                scaledH = (int) (params.height * metrics.density);
            } else if (Math.max(params.height, params.width) <= target) {
                scaledW = params.width;
                scaledH = params.height;
            } else if (params.width <= params.height) {
                scaledW = (int) (params.width / ((double) params.height / target));
                scaledH = (int) target;
            } else {
                scaledW = (int) target;
                scaledH = (int) (params.height / ((double) params.width / target));
            }
            final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(scaledW, scaledH);
            layoutParams.setMargins(0, (int) (metrics.density * 4), 0, (int) (metrics.density * 4));
            viewHolder.images.setLayoutParams(layoutParams);
            activity.loadGif(file, viewHolder.gifImage);
            viewHolder.gifImage.setOnClickListener(v -> openDownloadable(message));
        } else {
            showImages(true, mediaRuntime, false, viewHolder);
            FileParams params = message.getFileParams();
            final float target = activity.getResources().getDimension(R.dimen.image_preview_width);
            final int scaledW;
            final int scaledH;
            if (Math.max(params.height, params.width) * metrics.density <= target) {
                scaledW = (int) (params.width * metrics.density);
                scaledH = (int) (params.height * metrics.density);
            } else if (Math.max(params.height, params.width) <= target) {
                scaledW = params.width;
                scaledH = params.height;
            } else if (params.width <= params.height) {
                scaledW = (int) (params.width / ((double) params.height / target));
                scaledH = (int) target;
            } else {
                scaledW = (int) target;
                scaledH = (int) (params.height / ((double) params.width / target));
            }
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(scaledW, scaledH);
            layoutParams.setMargins(0, (int) (metrics.density * 4), 0, (int) (metrics.density * 4));
            viewHolder.images.setLayoutParams(layoutParams);
            activity.loadBitmap(message, viewHolder.image);
            viewHolder.image.setOnClickListener(v -> openDownloadable(message));
        }
    }

    private void imagePreviewLayout(int w, int h, ImageView image) {
        final float target = activity.getResources().getDimension(R.dimen.image_preview_width);
        final int scaledW;
        final int scaledH;
        if (Math.max(h, w) * metrics.density <= target) {
            scaledW = (int) (w * metrics.density);
            scaledH = (int) (h * metrics.density);
        } else if (Math.max(h, w) <= target) {
            scaledW = w;
            scaledH = h;
        } else if (w <= h) {
            scaledW = (int) (w / ((double) h / target));
            scaledH = (int) target;
        } else {
            scaledW = (int) target;
            scaledH = (int) (h / ((double) w / target));
        }
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(scaledW, scaledH);
        layoutParams.setMargins(0, (int) (metrics.density * 4), 0, (int) (metrics.density * 4));
        image.setLayoutParams(layoutParams);
    }

    private void showImages(final boolean show, final ViewHolder viewHolder) {
        showImages(show, 0, false, viewHolder);
    }

    private void showImages(final boolean show, final int duration, final boolean isGif, final ViewHolder viewHolder) {
        boolean hasDuration = duration > 0;
        if (show) {
            viewHolder.images.setVisibility(View.VISIBLE);
            if (hasDuration) {
                viewHolder.mediaduration.setVisibility(View.VISIBLE);
                viewHolder.mediaduration.setText(formatTime(safeLongToInt(duration)));
            } else {
                viewHolder.mediaduration.setVisibility(View.GONE);
            }
            if (isGif) {
                viewHolder.image.setVisibility(View.GONE);
                viewHolder.gifImage.setVisibility(View.VISIBLE);
            } else {
                viewHolder.image.setVisibility(View.VISIBLE);
                viewHolder.gifImage.setVisibility(View.GONE);
            }
        } else {
            viewHolder.images.setVisibility(View.GONE);
            viewHolder.image.setVisibility(View.GONE);
            viewHolder.gifImage.setVisibility(View.GONE);
            viewHolder.mediaduration.setVisibility(View.GONE);
        }
    }

    private void toggleWhisperInfo(ViewHolder viewHolder, final Message message,
                                   final boolean includeBody, final boolean darkBackground) {
        SpannableStringBuilder messageBody = new SpannableStringBuilder(replaceYoutube(activity.getApplicationContext(), message.getBody()));

        final String mimeType = message.getMimeType();
        if (mimeType != null && message.getMimeType().contains("audio")) {
            messageBody.clear();
            messageBody.append(getTitle(message));
        }
        Editable body;
        if (darkBackground) {
            viewHolder.messageBody.setTextAppearance(getContext(), R.style.TextAppearance_Conversations_Body1_OnDark);
        } else {
            viewHolder.messageBody.setTextAppearance(getContext(), R.style.TextAppearance_Conversations_Body1);
        }
        if (message.isPrivateMessage()) {
            final String privateMarker;
            if (message.getStatus() <= Message.STATUS_RECEIVED) {
                privateMarker = activity.getString(R.string.private_message);
            } else {
                Jid cp = message.getCounterpart();
                privateMarker = activity.getString(R.string.private_message_to, Strings.nullToEmpty(cp == null ? null : cp.getResource()));
            }
            body = new SpannableStringBuilder(privateMarker);
            viewHolder.messageBody.setVisibility(View.VISIBLE);
            if (includeBody) {
                body.append("\n");
                body.append(messageBody);
            }
            body.setSpan(new ForegroundColorSpan(ThemeHelper.getMessageTextColorPrivate(activity)), 0, privateMarker.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            body.setSpan(new StyleSpan(Typeface.BOLD), 0, privateMarker.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            MyLinkify.addLinks(body, false);
            viewHolder.messageBody.setText(body);
            viewHolder.messageBody.setAutoLinkMask(0);
            viewHolder.messageBody.setTextIsSelectable(true);
            viewHolder.messageBody.setMovementMethod(ClickableMovementMethod.getInstance());
        } else {
            if (includeBody) {
                viewHolder.messageBody.setVisibility(View.VISIBLE);
                body = new SpannableStringBuilder(messageBody);
                MyLinkify.addLinks(body, false);
                viewHolder.messageBody.setText(body);
                viewHolder.messageBody.setAutoLinkMask(0);
                viewHolder.messageBody.setTextIsSelectable(true);
                viewHolder.messageBody.setMovementMethod(ClickableMovementMethod.getInstance());
            } else {
                viewHolder.messageBody.setVisibility(View.GONE);
            }
        }
    }

    private void loadMoreMessages(Conversation conversation) {
        conversation.setLastClearHistory(0, null);
        activity.xmppConnectionService.updateConversation(conversation);
        conversation.setHasMessagesLeftOnServer(true);
        conversation.setFirstMamReference(null);
        long timestamp = conversation.getLastMessageTransmitted().getTimestamp();
        if (timestamp == 0) {
            timestamp = System.currentTimeMillis();
        }
        conversation.messagesLoaded.set(true);
        MessageArchiveService.Query query = activity.xmppConnectionService.getMessageArchiveService().query(conversation, new MamReference(0), timestamp, false);
        if (query != null) {
            ToastCompat.makeText(activity, R.string.fetching_history_from_server, ToastCompat.LENGTH_LONG).show();
        } else {
            ToastCompat.makeText(activity, R.string.not_fetching_history_retention_period, ToastCompat.LENGTH_SHORT).show();
        }
    }

    @SuppressLint({"StringFormatInvalid", "ClickableViewAccessibility"})
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final Message message = getItem(position);
        final boolean omemoEncryption = message.getEncryption() == Message.ENCRYPTION_AXOLOTL;
        final boolean isInValidSession = message.isValidInSession() && (!omemoEncryption || message.isTrusted());
        final Conversational conversation = message.getConversation();
        final Account account = conversation.getAccount();
        final List<Element> commands = message.getCommands();
        final int type = getItemViewType(position);
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            switch (type) {
                case DATE_SEPARATOR:
                    view = activity.getLayoutInflater().inflate(R.layout.message_date_bubble, parent, false);
                    viewHolder.status_message = view.findViewById(R.id.status_message);
                    viewHolder.message_box = view.findViewById(R.id.message_box);
                    viewHolder.indicatorReceived = view.findViewById(R.id.indicator_received);
                    break;
                case RTP_SESSION:
                    view = activity.getLayoutInflater().inflate(R.layout.message_rtp_session, parent, false);
                    viewHolder.status_message = view.findViewById(R.id.message_body);
                    viewHolder.message_box = view.findViewById(R.id.message_box);
                    viewHolder.indicatorReceived = view.findViewById(R.id.indicator_received);
                    break;
                case SENT:
                    view = activity.getLayoutInflater().inflate(R.layout.message_sent, parent, false);
                    viewHolder.message_box = view.findViewById(R.id.message_box);
                    viewHolder.contact_picture = view.findViewById(R.id.message_photo);
                    viewHolder.username = view.findViewById(R.id.username);
                    viewHolder.audioPlayer = view.findViewById(R.id.audio_player);
                    viewHolder.download_button = view.findViewById(R.id.download_button);
                    viewHolder.resend_button = view.findViewById(R.id.resend_button);
                    viewHolder.indicator = view.findViewById(R.id.security_indicator);
                    viewHolder.edit_indicator = view.findViewById(R.id.edit_indicator);
                    viewHolder.retract_indicator = view.findViewById(R.id.retract_indicator);
                    viewHolder.images = view.findViewById(R.id.images);
                    viewHolder.mediaduration = view.findViewById(R.id.media_duration);
                    viewHolder.image = view.findViewById(R.id.message_image);
                    viewHolder.gifImage = view.findViewById(R.id.message_image_gif);
                    viewHolder.richlinkview = view.findViewById(R.id.richLinkView);
                    viewHolder.messageBody = view.findViewById(R.id.message_body);
                    viewHolder.user = view.findViewById(R.id.message_user);
                    viewHolder.time = view.findViewById(R.id.message_time);
                    viewHolder.subject = view.findViewById(R.id.message_subject);
                    viewHolder.indicatorReceived = view.findViewById(R.id.indicator_received);
                    viewHolder.transfer = view.findViewById(R.id.transfer);
                    viewHolder.progressBar = view.findViewById(R.id.progressBar);
                    viewHolder.cancel_transfer = view.findViewById(R.id.cancel_transfer);
                    viewHolder.thread_identicon = view.findViewById(R.id.thread_identicon);
                    break;
                case RECEIVED:
                    view = activity.getLayoutInflater().inflate(R.layout.message_received, parent, false);
                    viewHolder.message_box = view.findViewById(R.id.message_box);
                    viewHolder.contact_picture = view.findViewById(R.id.message_photo);
                    viewHolder.username = view.findViewById(R.id.username);
                    viewHolder.audioPlayer = view.findViewById(R.id.audio_player);
                    viewHolder.download_button = view.findViewById(R.id.download_button);
                    viewHolder.answer_button = view.findViewById(R.id.answer);
                    viewHolder.indicator = view.findViewById(R.id.security_indicator);
                    viewHolder.edit_indicator = view.findViewById(R.id.edit_indicator);
                    viewHolder.retract_indicator = view.findViewById(R.id.retract_indicator);
                    viewHolder.images = view.findViewById(R.id.images);
                    viewHolder.mediaduration = view.findViewById(R.id.media_duration);
                    viewHolder.image = view.findViewById(R.id.message_image);
                    viewHolder.gifImage = view.findViewById(R.id.message_image_gif);
                    viewHolder.richlinkview = view.findViewById(R.id.richLinkView);
                    viewHolder.messageBody = view.findViewById(R.id.message_body);
                    viewHolder.user = view.findViewById(R.id.message_user);
                    viewHolder.time = view.findViewById(R.id.message_time);
                    viewHolder.subject = view.findViewById(R.id.message_subject);
                    viewHolder.indicatorReceived = view.findViewById(R.id.indicator_received);
                    viewHolder.encryption = view.findViewById(R.id.message_encryption);
                    viewHolder.transfer = view.findViewById(R.id.transfer);
                    viewHolder.progressBar = view.findViewById(R.id.progressBar);
                    viewHolder.cancel_transfer = view.findViewById(R.id.cancel_transfer);
                    viewHolder.commands_list = view.findViewById(R.id.commands_list);
                    viewHolder.thread_identicon = view.findViewById(R.id.thread_identicon);
                    break;
                case STATUS:
                    view = activity.getLayoutInflater().inflate(R.layout.message_status, parent, false);
                    viewHolder.contact_picture = view.findViewById(R.id.message_photo);
                    viewHolder.status_message = view.findViewById(R.id.status_message);
                    viewHolder.load_more_messages = view.findViewById(R.id.load_more_messages);
                    break;
                default:
                    throw new AssertionError("Unknown view type");
            }
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
            if (viewHolder == null) {
                return view;
            }
        }

        if (viewHolder.thread_identicon != null) {
            viewHolder.thread_identicon.setVisibility(View.GONE);
            final Element thread = message.getThread();
            if (thread != null) {
                final String threadId = thread.getContent();
                if (threadId != null) {
                    viewHolder.thread_identicon.setVisibility(View.VISIBLE);
                    viewHolder.thread_identicon.setColor(UIHelper.getColorForName(threadId));
                    viewHolder.thread_identicon.setHash(UIHelper.identiconHash(threadId));
                }
            }
        }

        boolean darkBackground = activity.isDarkTheme();

        if (type == DATE_SEPARATOR) {
            if (UIHelper.today(message.getTimeSent())) {
                viewHolder.status_message.setText(R.string.today);
            } else if (UIHelper.yesterday(message.getTimeSent())) {
                viewHolder.status_message.setText(R.string.yesterday);
            } else {
                viewHolder.status_message.setText(DateUtils.formatDateTime(activity, message.getTimeSent(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
            }
            viewHolder.message_box.setBackgroundResource(darkBackground ? R.drawable.date_bubble_dark : R.drawable.date_bubble);
            int date_bubble_color = ColorUtils.setAlphaComponent(StyledAttributes.getColor(activity, R.attr.colorAccent), 80); //set alpha to date_bubble
            activity.setBubbleColor(viewHolder.message_box, (date_bubble_color), -1); // themed color
            return view;
        } else if (type == RTP_SESSION) {
            final boolean isDarkTheme = activity.isDarkTheme();
            final boolean received = message.getStatus() <= Message.STATUS_RECEIVED;
            final String formattedTime = UIHelper.readableTimeDifferenceFull(activity, message.getMergedTimeSent());
            final RtpSessionStatus rtpSessionStatus = RtpSessionStatus.of(message.getBody());
            final long duration = rtpSessionStatus.duration;
            if (received) {
                if (duration > 0) {
                    viewHolder.status_message.setText(activity.getString(R.string.incoming_call_duration_timestamp, TimeFrameUtils.resolve(activity, duration), UIHelper.readableTimeDifferenceFull(activity, message.getTimeSent())));
                } else {
                    viewHolder.status_message.setText(activity.getString(R.string.missed_call_timestamp, formattedTime));
                }
            } else {
                if (duration > 0) {
                    viewHolder.status_message.setText(activity.getString(R.string.outgoing_call_duration_timestamp, TimeFrameUtils.resolve(activity, duration), UIHelper.readableTimeDifferenceFull(activity, message.getTimeSent())));
                } else {
                    viewHolder.status_message.setText(activity.getString(R.string.outgoing_call_timestamp, UIHelper.readableTimeDifferenceFull(activity, message.getTimeSent())));
                }
            }
            viewHolder.indicatorReceived.setImageResource(RtpSessionStatus.getDrawable(received, rtpSessionStatus.successful, isDarkTheme));
            viewHolder.indicatorReceived.setAlpha(isDarkTheme ? 0.7f : 0.57f);
            viewHolder.message_box.setBackgroundResource(darkBackground ? R.drawable.date_bubble_dark : R.drawable.date_bubble);
            int date_bubble_color = ColorUtils.setAlphaComponent(StyledAttributes.getColor(activity, R.attr.colorAccent), 80); //set alpha to date bubble
            activity.setBubbleColor(viewHolder.message_box, (date_bubble_color), -1); //themed color
            return view;
        } else if (type == STATUS) {
            if ("LOAD_MORE".equals(message.getBody())) {
                viewHolder.status_message.setVisibility(View.GONE);
                viewHolder.contact_picture.setVisibility(View.GONE);
                viewHolder.load_more_messages.setVisibility(View.VISIBLE);
                viewHolder.load_more_messages.setOnClickListener(v -> loadMoreMessages((Conversation) message.getConversation()));
            } else {
                viewHolder.status_message.setVisibility(View.VISIBLE);
                viewHolder.load_more_messages.setVisibility(View.GONE);
                viewHolder.status_message.setText(message.getBody());
                boolean showAvatar;
                if (conversation.getMode() == Conversation.MODE_SINGLE) {
                    showAvatar = true;
                    AvatarWorkerTask.loadAvatar(message, viewHolder.contact_picture, R.dimen.avatar_on_status_message);
                } else if (message.getCounterpart() != null || message.getTrueCounterpart() != null || (message.getCounterparts() != null && message.getCounterparts().size() > 0)) {
                    showAvatar = true;
                    AvatarWorkerTask.loadAvatar(message, viewHolder.contact_picture, R.dimen.avatar_on_status_message);
                } else {
                    showAvatar = false;
                }
                if (showAvatar) {
                    viewHolder.contact_picture.setAlpha(0.5f);
                    viewHolder.contact_picture.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.contact_picture.setVisibility(View.GONE);
                }
            }
            return view;
        } else {
            AvatarWorkerTask.loadAvatar(message, viewHolder.contact_picture, R.dimen.avatar);
        }

        resetClickListener(viewHolder.message_box, viewHolder.messageBody);

        viewHolder.message_box.setOnClickListener(v -> {
            if (MessageAdapter.this.mOnMessageBoxClickedListener != null) {
                MessageAdapter.this.mOnMessageBoxClickedListener
                        .onContactPictureClicked(message);
            }
        });
        viewHolder.messageBody.setOnClickListener(v -> {
            if (MessageAdapter.this.mOnMessageBoxClickedListener != null) {
                MessageAdapter.this.mOnMessageBoxClickedListener
                        .onContactPictureClicked(message);
            }
        });
        viewHolder.contact_picture.setOnClickListener(v -> {
            if (MessageAdapter.this.mOnContactPictureClickedListener != null) {
                MessageAdapter.this.mOnContactPictureClickedListener.onContactPictureClicked(message);
            }
        });
        SwipeDetector swipeDetector = new SwipeDetector((action) -> {
            if (action == SwipeDetector.Action.LR && MessageAdapter.this.mOnMessageBoxSwipedListener != null) {
                MessageAdapter.this.mOnMessageBoxSwipedListener.onContactPictureClicked(message);
            }
        });
        viewHolder.message_box.setOnTouchListener(swipeDetector);
        viewHolder.messageBody.setOnTouchListener(swipeDetector);
        viewHolder.image.setOnTouchListener(swipeDetector);
        viewHolder.time.setOnTouchListener(swipeDetector);
        viewHolder.contact_picture.setOnLongClickListener(v -> {
            if (MessageAdapter.this.mOnContactPictureLongClickedListener != null) {
                MessageAdapter.this.mOnContactPictureLongClickedListener.onContactPictureLongClicked(v, message);
                return true;
            } else {
                return false;
            }
        });

        final Transferable transferable = message.getTransferable();
        final boolean unInitiatedButKnownSize = MessageUtils.unInitiatedButKnownSize(message);
        if (unInitiatedButKnownSize || message.isFileDeleted() || (transferable != null && transferable.getStatus() != Transferable.STATUS_UPLOADING)) {
            if (unInitiatedButKnownSize || transferable != null && transferable.getStatus() == Transferable.STATUS_OFFER) {
                displayDownloadableMessage(viewHolder, message, activity.getString(R.string.download_x_file, UIHelper.getFileDescriptionString(activity, message)), darkBackground, type);
            } else if (transferable != null && transferable.getStatus() == Transferable.STATUS_OFFER_CHECK_FILESIZE) {
                displayDownloadableMessage(viewHolder, message, activity.getString(R.string.check_x_filesize, UIHelper.getFileDescriptionString(activity, message)), darkBackground, type);
            } else {
                /* todo why should we mark a file as deleted? --> causing strange side effects
                if (!activity.xmppConnectionService.getFileBackend().getFile(message).exists() && !message.isFileDeleted()) {
                    new Thread(new markFileDeletedFinisher(message, activity)).start();
                    displayInfoMessage(viewHolder, activity.getString(R.string.file_deleted), darkBackground, message);
                }*/
                if (checkFileExistence(message, view, viewHolder)) {
                    new Thread(new markFileExistingFinisher(message, activity)).start();
                }
                displayInfoMessage(viewHolder, UIHelper.getMessagePreview(activity, message).first, darkBackground, message, type);
            }
        } else if (message.isFileOrImage() && message.getEncryption() != Message.ENCRYPTION_PGP && message.getEncryption() != Message.ENCRYPTION_DECRYPTION_FAILED) {
            if (message.getFileParams().width > 0 && message.getFileParams().height > 0) {
                displayMediaPreviewMessage(viewHolder, message, darkBackground, type);
            } else if (message.getFileParams().runtime > 0 && (message.getFileParams().width == 0 && message.getFileParams().height == 0)) {
                displayAudioMessage(viewHolder, message, darkBackground, type);
            } else if ("application/xdc+zip".equals(message.getFileParams().getMediaType()) && message.getConversation() instanceof Conversation && message.getThread() != null) {
                displayWebxdcMessage(viewHolder, message, darkBackground, type);
            } else {
                displayOpenableMessage(viewHolder, message, darkBackground, type);
            }
        } else if (message.getEncryption() == Message.ENCRYPTION_PGP) {
            if (account.isPgpDecryptionServiceConnected()) {
                if (conversation instanceof Conversation && !account.hasPendingPgpIntent((Conversation) conversation)) {
                    displayInfoMessage(viewHolder, activity.getString(R.string.message_decrypting), darkBackground, message);
                } else {
                    displayInfoMessage(viewHolder, activity.getString(R.string.pgp_message), darkBackground, message);
                }
            } else {
                displayInfoMessage(viewHolder, activity.getString(R.string.install_openkeychain), darkBackground, message);
                viewHolder.message_box.setOnClickListener(this::promptOpenKeychainInstall);
                viewHolder.messageBody.setOnClickListener(this::promptOpenKeychainInstall);
            }
        } else if (message.getEncryption() == Message.ENCRYPTION_DECRYPTION_FAILED) {
            displayInfoMessage(viewHolder, activity.getString(R.string.decryption_failed), darkBackground, message);
        } else if (message.getEncryption() == Message.ENCRYPTION_AXOLOTL_NOT_FOR_THIS_DEVICE) {
            displayInfoMessage(viewHolder, activity.getString(R.string.not_encrypted_for_this_device), darkBackground, message);
        } else if (message.getEncryption() == Message.ENCRYPTION_AXOLOTL_FAILED) {
            displayInfoMessage(viewHolder, activity.getString(R.string.omemo_decryption_failed), darkBackground, message);
        } else {
            if (message.isGeoUri()) {
                displayLocationMessage(viewHolder, message, darkBackground, type);
            } else if (message.isXmppUri()) {
                displayXmppMessage(viewHolder, message.getBody().trim());
            } else if (message.treatAsDownloadable()) {
                try {
                    final URI uri = message.getOob();
                    displayDownloadableMessage(viewHolder,
                            message,
                            activity.getString(R.string.check_x_filesize_on_host,
                                    UIHelper.getFileDescriptionString(activity, message),
                                    uri.getHost()),
                            darkBackground, type);
                } catch (Exception e) {
                    displayDownloadableMessage(viewHolder,
                            message,
                            activity.getString(R.string.check_x_filesize,
                                    UIHelper.getFileDescriptionString(activity, message)),
                            darkBackground, type);
                }
            } else if (message.bodyIsOnlyEmojis() && message.getType() != Message.TYPE_PRIVATE) {
                displayEmojiMessage(viewHolder, message.getBody().trim(), darkBackground);
            } else {
                displayTextMessage(viewHolder, message, darkBackground, type);
            }
        }

        if (type == RECEIVED) {
            if (commands != null && conversation instanceof Conversation) {
                CommandButtonAdapter adapter = new CommandButtonAdapter(activity);
                adapter.addAll(commands);
                viewHolder.commands_list.setAdapter(adapter);
                viewHolder.commands_list.setVisibility(View.VISIBLE);
                viewHolder.commands_list.setOnItemClickListener((p, v, pos, id) -> {
                    final Element command = adapter.getItem(pos);
                    activity.startCommand(conversation.getAccount(), command.getAttributeAsJid("jid"), command.getAttribute("node"));
                });
            } else {
                // It's unclear if we can set this to null...
                ListAdapter adapter = viewHolder.commands_list.getAdapter();
                if (adapter instanceof ArrayAdapter) {
                    ((ArrayAdapter<?>) adapter).clear();
                }
                viewHolder.commands_list.setVisibility(View.GONE);
                viewHolder.commands_list.setOnItemClickListener(null);
            }
            if (message.isPrivateMessage()) {
                viewHolder.answer_button.setVisibility(View.VISIBLE);
                Drawable icon = activity.getResources().getDrawable(R.drawable.ic_reply_circle_black_24dp);
                Drawable drawable = DrawableCompat.wrap(icon);
                DrawableCompat.setTint(drawable, StyledAttributes.getColor(getContext(), R.attr.colorAccent));
                viewHolder.answer_button.setImageDrawable(drawable);
                viewHolder.answer_button.setOnClickListener(v -> {
                    try {
                        if (activity instanceof ConversationsActivity) {
                            ConversationFragment conversationFragment = ConversationFragment.get(activity);
                            if (conversationFragment != null) {
                                activity.invalidateOptionsMenu();
                                conversationFragment.privateMessageWith(message.getCounterpart());
                            }
                        }
                    } catch (Exception e) {
                        viewHolder.answer_button.setVisibility(View.GONE);
                        e.printStackTrace();
                    }
                });
            } else {
                viewHolder.answer_button.setVisibility(View.GONE);
            }
            if (isInValidSession) {
                setBubbleBackgroundColor(viewHolder.message_box, type, message.isPrivateMessage(), isInValidSession);
                viewHolder.encryption.setVisibility(View.GONE);
                viewHolder.encryption.setTextColor(ThemeHelper.getMessageTextColor(activity, darkBackground, false));
            } else {
                setBubbleBackgroundColor(viewHolder.message_box, type, message.isPrivateMessage(), isInValidSession);
                viewHolder.encryption.setVisibility(View.VISIBLE);
                viewHolder.encryption.setTextColor(ThemeHelper.getWarningTextColor(activity, darkBackground));
                if (omemoEncryption && !message.isTrusted()) {
                    viewHolder.encryption.setText(R.string.not_trusted);
                } else {
                    viewHolder.encryption.setText(CryptoHelper.encryptionTypeToText(message.getEncryption()));
                }
            }
        }
        if (type == RECEIVED || type == SENT) {
            if (message.getSubject() == null) {
                viewHolder.subject.setVisibility(View.GONE);
            } else {
                viewHolder.subject.setVisibility(View.VISIBLE);
                viewHolder.subject.setText(message.getSubject());
            }
        }

        if (type == SENT) {
            setBubbleBackgroundColor(viewHolder.message_box, type, message.isPrivateMessage(), isInValidSession);
        }
        displayStatus(viewHolder, message, type, darkBackground);
        showAvatar(viewHolder, message, view);
        return view;
    }
    private void showAvatar(ViewHolder viewHolder, Message message, View view) {
        if (message.isAvatarable()) {
            viewHolder.contact_picture.setVisibility(View.VISIBLE);
            int left = ThemeHelper.dp2Px(getContext(), 8);
            int top = ThemeHelper.dp2Px(getContext(), 0);
            int right = ThemeHelper.dp2Px(getContext(), 8);
            int bottom = ThemeHelper.dp2Px(getContext(), 16);
            view.setPadding(left, top, right, bottom);
        } else {
            viewHolder.contact_picture.setVisibility(View.INVISIBLE);
            int left = ThemeHelper.dp2Px(getContext(), 8);
            int top = ThemeHelper.dp2Px(getContext(), 0);
            int right = ThemeHelper.dp2Px(getContext(), 8);
            int bottom = ThemeHelper.dp2Px(getContext(), 1);
            view.setPadding(left, top, right, bottom);
        }
    }

    private static class markFileExistingFinisher implements Runnable {
        private final Message message;
        private final WeakReference<XmppActivity> activityReference;

        private markFileExistingFinisher(Message message, XmppActivity activity) {
            this.message = message;
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            final XmppActivity activity = activityReference.get();
            if (activity == null) {
                return;
            }
            activity.runOnUiThread(
                    () -> {
                        Log.d(Config.LOGTAG, "Found and restored orphaned file " + message.getRelativeFilePath());
                        message.setFileDeleted(false);
                        activity.xmppConnectionService.updateMessage(message, false);
                        activity.xmppConnectionService.updateConversation((Conversation) message.getConversation());
                    });
        }
    }

    private static class markFileDeletedFinisher implements Runnable {
        private final Message message;
        private final WeakReference<XmppActivity> activityReference;

        private markFileDeletedFinisher(Message message, XmppActivity activity) {
            this.message = message;
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            final XmppActivity activity = activityReference.get();
            if (activity == null) {
                return;
            }
            activity.runOnUiThread(
                    () -> {
                        Log.d(Config.LOGTAG, "Mark file deleted " + message.getRelativeFilePath());
                        message.setFileDeleted(true);
                        activity.xmppConnectionService.updateMessage(message, false);
                        activity.xmppConnectionService.updateConversation((Conversation) message.getConversation());
                    });
        }
    }

    private boolean checkFileExistence(Message message, View view, ViewHolder viewHolder) {
        final Rect scrollBounds = new Rect();
        view.getHitRect(scrollBounds);
        if (message.isFileDeleted() && viewHolder.messageBody.getLocalVisibleRect(scrollBounds)) {
            return activity.xmppConnectionService.getFileBackend().getFile(message).exists();
        } else {
            return false;
        }
    }

    private void promptOpenKeychainInstall(View view) {
        activity.showInstallPgpDialog();
    }

    public FileBackend getFileBackend() {
        return activity.xmppConnectionService.getFileBackend();
    }

    public void stopAudioPlayer() {
        audioPlayer.stop();
    }

    public void unregisterListenerInAudioPlayer() {
        audioPlayer.unregisterListener();
    }

    public void startStopPending() {
        audioPlayer.startStopPending();
    }

    public void openDownloadable(Message message) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ConversationFragment.registerPendingMessage(activity, message);
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, ConversationsActivity.REQUEST_OPEN_MESSAGE);
            return;
        }
        final DownloadableFile file = activity.xmppConnectionService.getFileBackend().getFile(message);
        ViewUtil.view(activity, file);
    }

    private void showLocation(Message message) {
        for (Intent intent : GeoHelper.createGeoIntentsFromMessage(this.getContext(), message)) {
            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                getContext().startActivity(intent);
                activity.overridePendingTransition(R.animator.fade_in, R.animator.fade_out);
                return;
            }
        }
    }

    public void updatePreferences() {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(activity);
        this.mIndicateReceived = p.getBoolean("indicate_received", activity.getResources().getBoolean(R.bool.indicate_received));
        this.mPlayGifInside = p.getBoolean(PLAY_GIF_INSIDE, activity.getResources().getBoolean(R.bool.play_gif_inside));
        this.mShowLinksInside = p.getBoolean(SHOW_LINKS_INSIDE, activity.getResources().getBoolean(R.bool.show_links_inside));
        this.mShowMapsInside = p.getBoolean(SHOW_MAPS_INSIDE, activity.getResources().getBoolean(R.bool.show_maps_inside));
    }

    public void setHighlightedTerm(List<String> terms) {
        this.highlightedTerm = terms == null ? null : StylingHelper.filterHighlightedWords(terms);
    }

    public interface OnQuoteListener {
        void onQuote(String text, String user);
    }

    public interface OnContactPictureClicked {
        void onContactPictureClicked(Message message);
    }

    public interface OnContactPictureLongClicked {
        void onContactPictureLongClicked(View v, Message message);
    }

    public interface OnInlineImageLongClicked {
        boolean onInlineImageLongClicked(Cid cid);
    }


    private static class ViewHolder {

        public Button load_more_messages;
        public ImageView edit_indicator;
        public ImageView retract_indicator;
        public RelativeLayout audioPlayer;
        public RelativeLayout images;
        protected LinearLayout message_box;
        protected Button download_button;
        protected Button resend_button;
        protected ImageButton answer_button;
        protected ImageView image;
        protected GifImageView gifImage;
        protected TextView mediaduration;
        protected RichLinkView richlinkview;
        protected ImageView indicator;
        protected ImageView indicatorReceived;
        protected TextView time;
        protected TextView subject;
        protected TextView messageBody;
        protected TextView user;
        protected TextView username;
        protected ImageView contact_picture;
        protected TextView status_message;
        protected TextView encryption;
        protected ListView commands_list;
        protected GithubIdenticonView thread_identicon;
        protected RelativeLayout transfer;
        protected ProgressBar progressBar;
        protected ImageButton cancel_transfer;
    }

    public void setBubbleBackgroundColor(final View viewHolder, final int type,
                                         final boolean isPrivateMessage, final boolean isInValidSession) {
        if (type == RECEIVED) {
            if (isInValidSession) {
                if (isPrivateMessage) {
                    viewHolder.setBackgroundResource(R.drawable.message_bubble_received_light_private);
                    activity.setBubbleColor(viewHolder, StyledAttributes.getColor(activity, R.attr.color_bubble_light), StyledAttributes.getColor(activity, R.attr.colorAccent));
                } else {
                    viewHolder.setBackgroundResource(R.drawable.message_bubble_received_light);
                    activity.setBubbleColor(viewHolder, StyledAttributes.getColor(activity, R.attr.color_bubble_light), -1);
                }
            } else {
                if (isPrivateMessage) {
                    viewHolder.setBackgroundResource(R.drawable.message_bubble_received_warning_private);
                    activity.setBubbleColor(viewHolder, StyledAttributes.getColor(activity, R.attr.color_bubble_warning), StyledAttributes.getColor(activity, R.attr.colorAccent));
                } else {
                    viewHolder.setBackgroundResource(R.drawable.message_bubble_received_warning);
                    activity.setBubbleColor(viewHolder, StyledAttributes.getColor(activity, R.attr.color_bubble_warning), -1);
                }
            }
        }

        if (type == SENT) {
            if (isPrivateMessage) {
                viewHolder.setBackgroundResource(R.drawable.message_bubble_sent_private);
                activity.setBubbleColor(viewHolder, StyledAttributes.getColor(activity, R.attr.color_bubble_dark), StyledAttributes.getColor(activity, R.attr.colorAccent));
            } else {
                viewHolder.setBackgroundResource(R.drawable.message_bubble_sent);
                activity.setBubbleColor(viewHolder, StyledAttributes.getColor(activity, R.attr.color_bubble_dark), -1);
            }
        }
    }

    class ThumbnailTask extends AsyncTask<DownloadableFile, Void, Drawable[]> {
        @Override
        protected Drawable[] doInBackground(DownloadableFile... params) {
            if (isCancelled()) return null;

            Drawable[] d = new Drawable[params.length];
            for (int i = 0; i < params.length; i++) {
                try {
                    d[i] = activity.xmppConnectionService.getFileBackend().getThumbnail(params[i], activity.getResources(), (int) (metrics.density * 288), false);
                } catch (final IOException e) {
                    d[i] = null;
                }
            }

            return d;
        }

        @Override
        protected void onPostExecute(final Drawable[] d) {
            if (isCancelled()) return;
            activity.xmppConnectionService.updateConversationUi();
        }
    }
}
