package eu.siacs.conversations.utils;

import static eu.siacs.conversations.services.EventReceiver.EXTRA_NEEDS_FOREGROUND_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import androidx.annotation.BoolRes;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.List;

import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;
import eu.siacs.conversations.ui.SettingsActivity;
import eu.siacs.conversations.ui.SettingsFragment;

public class Compatibility {
    private static final List<String> UNUSED_SETTINGS_POST_TWENTYSIX = Arrays.asList(
            SettingsActivity.SHOW_FOREGROUND_SERVICE,
            "led",
            "notification_ringtone",
            "notification_headsup",
            "vibrate_on_notification",
            "call_ringtone"
    );

    private static final List<String> UNUSED_SETTINGS_PRE_TWENTYSIX = Arrays.asList(
            "message_notification_settings",
            "call_notification_settings",
            "remove_all_individual_notifications"
    );

    public static boolean hasStoragePermission(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean runsTwentySix() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static boolean runsTwentyFour() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean twentyEight() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }

    private static boolean getBooleanPreference(Context context, String name, @BoolRes int res) {
        return getPreferences(context).getBoolean(name, context.getResources().getBoolean(res));
    }

    private static SharedPreferences getPreferences(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static boolean targetsTwentySix(Context context) {
        try {
            final PackageManager packageManager = context.getPackageManager();
            final ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            return applicationInfo == null || applicationInfo.targetSdkVersion >= 26;
        } catch (PackageManager.NameNotFoundException e) {
            return true; //when in doubt…
        } catch (RuntimeException e) {
            return true; //when in doubt…
        }
    }

    private static boolean targetsTwentyFour(Context context) {
        try {
            final PackageManager packageManager = context.getPackageManager();
            final ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            return applicationInfo == null || applicationInfo.targetSdkVersion >= 24;
        } catch (PackageManager.NameNotFoundException e) {
            return true; //when in doubt…
        } catch (RuntimeException e) {
            return true; //when in doubt…
        }
    }

    public static boolean runsAndTargetsTwentySix(Context context) {
        return runsTwentySix() && targetsTwentySix(context);
    }

    public static boolean runsAndTargetsTwentyFour(Context context) {
        return runsTwentyFour() && targetsTwentyFour(context);
    }

    public static boolean keepForegroundService(Context context) {
        return runsTwentySix() || getBooleanPreference(context, SettingsActivity.SHOW_FOREGROUND_SERVICE, R.bool.show_foreground_service);
    }

    public static void removeUnusedPreferences(SettingsFragment settingsFragment) {
        List<PreferenceScreen> screens = Arrays.asList(
                (PreferenceScreen) settingsFragment.findPreference("notifications"));
        List<PreferenceCategory> categories = Arrays.asList(
                (PreferenceCategory) settingsFragment.findPreference("general"));
        for (String key : (runsTwentySix() ? UNUSED_SETTINGS_POST_TWENTYSIX : UNUSED_SETTINGS_PRE_TWENTYSIX)) {
            Preference preference = settingsFragment.findPreference(key);
            if (preference != null) {
                for (PreferenceScreen screen : screens) {
                    if (screen != null) {
                        screen.removePreference(preference);
                    }
                }
                for (PreferenceCategory category : categories) {
                    if (category != null) {
                        category.removePreference(preference);
                    }
                }
            }
        }
        if (Compatibility.runsTwentySix()) {
            if (targetsTwentySix(settingsFragment.getContext())) {
                Preference preference = settingsFragment.findPreference(SettingsActivity.SHOW_FOREGROUND_SERVICE);
                if (preference != null) {
                    for (PreferenceCategory category : categories) {
                        if (category != null) {
                            category.removePreference(preference);
                        }
                    }
                }
            }
        }
    }

    public static void startService(Context context, Intent intent) {
        try {
            if (Compatibility.runsAndTargetsTwentySix(context)) {
                intent.putExtra(EXTRA_NEEDS_FOREGROUND_SERVICE, true);
                ContextCompat.startForegroundService(context, intent);
            } else {
                context.startService(intent);
            }
        } catch (RuntimeException e) {
            Log.d(Config.LOGTAG, context.getClass().getSimpleName() + " was unable to start service");
        }
    }

    @SuppressLint("UnsupportedChromeOsCameraSystemFeature")
    public static boolean hasFeatureCamera(final Context context) {
        final PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }
}