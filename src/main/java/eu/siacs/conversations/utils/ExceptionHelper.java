package eu.siacs.conversations.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ClassNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import eu.siacs.conversations.BuildConfig;
import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;
import eu.siacs.conversations.entities.Account;
import eu.siacs.conversations.entities.Conversation;
import eu.siacs.conversations.entities.Message;
import eu.siacs.conversations.services.XmppConnectionService;
import eu.siacs.conversations.ui.XmppActivity;

public class ExceptionHelper {
    private static final String FILENAME = "stacktrace.txt";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    public static void init(Context context) {
        if (Thread.getDefaultUncaughtExceptionHandler() instanceof ExceptionHandler) {
            return;
        }
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(context));
    }

    public static boolean checkForCrash(XmppActivity activity) {
        try {
            Class.forName("io.sentry.Sentry");
            return false;
        } catch (final ClassNotFoundException e) { }

        try {
            final XmppConnectionService service = activity == null ? null : activity.xmppConnectionService;
            if (service == null) {
                return false;
            }
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
            boolean crashreport = preferences.getBoolean("crashreport", activity.getResources().getBoolean(R.bool.send_crashreport));
            if (!crashreport || Config.BUG_REPORTS == null) {
                return false;
            }
            final Account account = AccountUtils.getFirstEnabled(service);
            if (account == null) {
                return false;
            }
            FileInputStream file = activity.openFileInput(FILENAME);
            InputStreamReader inputStreamReader = new InputStreamReader(file);
            BufferedReader stacktrace = new BufferedReader(inputStreamReader);
            final StringBuilder report = new StringBuilder();
            PackageManager pm = activity.getPackageManager();
            PackageInfo packageInfo;
            String release = Build.VERSION.RELEASE;
            int sdkVersion = Build.VERSION.SDK_INT;
            String deviceName = getDeviceName();
            try {
                packageInfo = pm.getPackageInfo(activity.getPackageName(), PackageManager.GET_SIGNATURES);
                report.append("Device: ").append(deviceName).append('\n');
                report.append("Android SDK: ").append(sdkVersion).append(" (").append(release).append(")").append('\n');
                report.append("Version: ").append(packageInfo.versionName).append('\n');
                report.append(String.format(Locale.ROOT, "Version: %s(%d) ", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)).append('\n');
                report.append("Last Update: ").append(DATE_FORMAT.format(new Date(packageInfo.lastUpdateTime))).append('\n');
                Signature[] signatures = packageInfo.signatures;
                if (signatures != null && signatures.length >= 1) {
                    report.append("SHA-1: ").append(CryptoHelper.getFingerprintCert(packageInfo.signatures[0].toByteArray())).append('\n');
                }
                report.append('\n');
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            String line;
            while ((line = stacktrace.readLine()) != null) {
                report.append(line);
                report.append('\n');
            }
            file.close();
            activity.deleteFile(FILENAME);
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(activity.getString(R.string.crash_report_title));
            builder.setMessage(activity.getText(R.string.crash_report_message));
            builder.setPositiveButton(activity.getText(R.string.send_now), (dialog, which) -> {
                Log.d(Config.LOGTAG, "using account=" + account.getJid().asBareJid() + " to send in stack trace");
                final Conversation conversation = service.findOrCreateConversation(account, Config.BUG_REPORTS, false, true);
                final Message message = new Message(conversation, report.toString(), Message.ENCRYPTION_NONE);
                service.sendMessage(message);
            });
            builder.setNegativeButton(activity.getText(R.string.send_never), (dialog, which) -> preferences.edit().putBoolean("never_send", true).apply());
            builder.create().show();
            return true;
        } catch (final IOException ignored) {
            return false;
        }
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    static void writeToStacktraceFile(Context context, String msg) {
        try {
            OutputStream os = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            os.write(msg.getBytes());
            os.flush();
            os.close();
        } catch (IOException ignored) {
        }
    }
}
