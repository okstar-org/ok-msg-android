package eu.siacs.conversations.ui;

import static eu.siacs.conversations.persistance.FileBackend.APP_DIRECTORY;
import static eu.siacs.conversations.utils.StorageHelper.getBackupDirectory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import static eu.siacs.conversations.utils.CameraUtils.showCameraChooser;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import eu.siacs.conversations.utils.CameraUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import android.provider.MediaStore;
import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;
import eu.siacs.conversations.crypto.OmemoSetting;
import eu.siacs.conversations.entities.Account;
import eu.siacs.conversations.persistance.FileBackend;
import eu.siacs.conversations.services.ExportBackupService;
import eu.siacs.conversations.services.MemorizingTrustManager;
import eu.siacs.conversations.ui.util.StyledAttributes;
import eu.siacs.conversations.utils.Compatibility;
import eu.siacs.conversations.utils.ThemeHelper;
import eu.siacs.conversations.utils.TimeFrameUtils;
import eu.siacs.conversations.xmpp.Jid;
import me.drakeet.support.toast.ToastCompat;
import eu.siacs.conversations.services.UnifiedPushDistributor;

public class SettingsActivity extends XmppActivity implements OnSharedPreferenceChangeListener {

    public static final String AWAY_WHEN_SCREEN_IS_OFF = "away_when_screen_off";
    public static final String TREAT_VIBRATE_AS_SILENT = "treat_vibrate_as_silent";
    public static final String DND_ON_SILENT_MODE = "dnd_on_silent_mode";
    public static final String MANUALLY_CHANGE_PRESENCE = "manually_change_presence";
    public static final String BLIND_TRUST_BEFORE_VERIFICATION = "btbv";
    public static final String AUTOMATIC_MESSAGE_DELETION = "automatic_message_deletion";
    public static final String AUTOMATIC_ATTACHMENT_DELETION = "automatic_attachment_deletion";
    public static final String BROADCAST_LAST_ACTIVITY = "last_activity";
    public static final String WARN_UNENCRYPTED_CHAT = "warn_unencrypted_chat";
    public static final String HIDE_YOU_ARE_NOT_PARTICIPATING = "hide_you_are_not_participating";
    public static final String HIDE_MEMORY_WARNING = "hide_memory_warning";
    public static final String THEME = "theme";
    public static final String THEME_COLOR = "theme_color";
    public static final String SHOW_DYNAMIC_TAGS = "show_dynamic_tags";
    public static final String OMEMO_SETTING = "omemo";
    public static final String SHOW_FOREGROUND_SERVICE = "show_foreground_service";
    public static final String USE_BUNDLED_EMOJIS = "use_bundled_emoji";
    public static final String ENABLE_MULTI_ACCOUNTS = "enable_multi_accounts";
    public static final String SHOW_OWN_ACCOUNTS = "show_own_accounts";
    public static final String QUICK_SHARE_ATTACHMENT_CHOICE = "quick_share_attachment_choice";
    public static final String NUMBER_OF_ACCOUNTS = "number_of_accounts";
    public static final String PLAY_GIF_INSIDE = "play_gif_inside";
    public static final String USE_INTERNAL_UPDATER = "use_internal_updater";
    public static final String SHOW_LINKS_INSIDE = "show_links_inside";
    public static final String SHOW_MAPS_INSIDE = "show_maps_inside";
    public static final String PREFER_XMPP_AVATAR = "prefer_xmpp_avatar";
    public static final String CHAT_STATES = "chat_states";
    public static final String FORBID_SCREENSHOTS = "screen_security";
    public static final String CONFIRM_MESSAGES = "confirm_messages";
    public static final String INDICATE_RECEIVED = "indicate_received";
    public static final String USE_INVIDIOUS = "use_invidious";
    public static final String USE_INNER_STORAGE = "use_inner_storage";
    public static final String INVIDIOUS_HOST = "invidious_host";
    public static final String MAPPREVIEW_HOST = "mappreview_host";
    public static final String ALLOW_MESSAGE_CORRECTION = "allow_message_correction";
    public static final String ALLOW_MESSAGE_RETRACTION = "allow_message_retraction";
    public static final String ENABLE_OTR_ENCRYPTION = "enable_otr_encryption";
    public static final String USE_UNICOLORED_CHATBG = "unicolored_chatbg";
    public static final String EASY_DOWNLOADER = "easy_downloader";
    public static final String MIN_ANDROID_SDK21_SHOWN = "min_android_sdk21_shown";
    public static final String INDIVIDUAL_NOTIFICATION_PREFIX = "individual_notification_set_";
    public static final String CAMERA_CHOICE = "camera_choice";
    public static final String PAUSE_VOICE = "pause_voice_on_move_from_ear";
    public static final String PERSISTENT_ROOM = "enable_persistent_rooms";
    public static final String MAX_RESEND_TIME = "max_resend_time";
    public static final String RESEND_DELAY = "resend_delay";

    public static final int REQUEST_CREATE_BACKUP = 0xbf8701;
    public static final int REQUEST_IMPORT_SETTINGS = 0xbf8702;
    Preference multiAccountPreference;
    Preference autoMessageExpiryPreference;
    Preference autoFileExpiryPreference;
    Preference BundledEmojiPreference;
    Preference QuickShareAttachmentChoicePreference;
    boolean isMultiAccountChecked = false;
    boolean isBundledEmojiChecked;
    boolean isQuickShareAttachmentChoiceChecked = false;
    private SettingsFragment mSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(ThemeHelper.find(this));
        setContentView(R.layout.activity_settings);
        FragmentManager fm = getFragmentManager();
        mSettingsFragment = (SettingsFragment) fm.findFragmentById(R.id.settings_content);
        if (mSettingsFragment == null
                || !mSettingsFragment.getClass().equals(SettingsFragment.class)) {
            mSettingsFragment = new SettingsFragment();
            fm.beginTransaction().replace(R.id.settings_content, mSettingsFragment).commit();
        }
        mSettingsFragment.setActivityIntent(getIntent());
        getWindow().getDecorView().setBackgroundColor(StyledAttributes.getColor(this, R.attr.color_background_secondary));
        setSupportActionBar(findViewById(R.id.toolbar));
        configureActionBar(getSupportActionBar());
    }

    @Override
    void onBackendConnected() {
        final Preference accountPreference =
                mSettingsFragment.findPreference(UnifiedPushDistributor.PREFERENCE_ACCOUNT);
        reconfigureUpAccountPreference(accountPreference);
    }

    private void reconfigureUpAccountPreference(final Preference preference) {
        final ListPreference listPreference;
        if (preference instanceof ListPreference) {
            listPreference = (ListPreference) preference;
        } else {
            return;
        }
        final List<CharSequence> accounts =
                ImmutableList.copyOf(
                        Lists.transform(
                                xmppConnectionService.getAccounts(),
                                a -> a.getJid().asBareJid().toEscapedString()));
        final ImmutableList.Builder<CharSequence> entries = new ImmutableList.Builder<>();
        final ImmutableList.Builder<CharSequence> entryValues = new ImmutableList.Builder<>();
        entries.add(getString(R.string.no_account_deactivated));
        entryValues.add("none");
        entries.addAll(accounts);
        entryValues.addAll(accounts);
        listPreference.setEntries(entries.build().toArray(new CharSequence[0]));
        listPreference.setEntryValues(entryValues.build().toArray(new CharSequence[0]));
        if (!accounts.contains(listPreference.getValue())) {
            listPreference.setValue("none");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        multiAccountPreference = mSettingsFragment.findPreference("enable_multi_accounts");
        if (multiAccountPreference != null) {
            isMultiAccountChecked = ((CheckBoxPreference) multiAccountPreference).isChecked();
            //handleMultiAccountChanges();
        }

        BundledEmojiPreference = mSettingsFragment.findPreference("use_bundled_emoji");
        if (BundledEmojiPreference != null) {
            isBundledEmojiChecked = ((CheckBoxPreference) BundledEmojiPreference).isChecked();
        }

        QuickShareAttachmentChoicePreference = mSettingsFragment.findPreference("quick_share_attachment_choice");
        if (QuickShareAttachmentChoicePreference != null) {
            QuickShareAttachmentChoicePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                refreshUiReal();
                return true;
            });
            isQuickShareAttachmentChoiceChecked = ((CheckBoxPreference) QuickShareAttachmentChoicePreference).isChecked();
        }

        changeOmemoSettingSummary();

        if (Config.FORCE_ORBOT) {
            PreferenceCategory connectionOptions = (PreferenceCategory) mSettingsFragment.findPreference("connection_options");
            PreferenceScreen expert = (PreferenceScreen) mSettingsFragment.findPreference("expert");
            if (connectionOptions != null) {
                expert.removePreference(connectionOptions);
            }
        }

        PreferenceScreen mainPreferenceScreen = (PreferenceScreen) mSettingsFragment.findPreference("main_screen");
        PreferenceScreen UIPreferenceScreen = (PreferenceScreen) mSettingsFragment.findPreference("userinterface");

        // this feature is only available on Huawei Android 6.
        PreferenceScreen huaweiPreferenceScreen =
                (PreferenceScreen) mSettingsFragment.findPreference("huawei");
        if (huaweiPreferenceScreen != null) {
            Intent intent = huaweiPreferenceScreen.getIntent();
            // remove when Api version is above M (Version 6.0) or if the intent is not callable
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M || !isCallable(intent)) {
                PreferenceCategory generalCategory =
                        (PreferenceCategory) mSettingsFragment.findPreference("general");
                generalCategory.removePreference(huaweiPreferenceScreen);
                if (generalCategory.getPreferenceCount() == 0) {
                    if (mainPreferenceScreen != null) {
                        mainPreferenceScreen.removePreference(generalCategory);
                    }
                }
            }
        }


        ListPreference automaticMessageDeletionList =
                (ListPreference) mSettingsFragment.findPreference(AUTOMATIC_MESSAGE_DELETION);
        if (automaticMessageDeletionList != null) {
            final int[] choices =
                    getResources().getIntArray(R.array.automatic_message_deletion_values);
            CharSequence[] entries = new CharSequence[choices.length];
            CharSequence[] entryValues = new CharSequence[choices.length];
            for (int i = 0; i < choices.length; ++i) {
                entryValues[i] = String.valueOf(choices[i]);
                if (choices[i] == 0) {
                    entries[i] = getString(R.string.never);
                } else {
                    entries[i] = TimeFrameUtils.resolve(this, 1000L * choices[i]);
                }
            }
            automaticMessageDeletionList.setEntries(entries);
            automaticMessageDeletionList.setEntryValues(entryValues);
        }

        ListPreference automaticAttachmentDeletionList = (ListPreference) mSettingsFragment.findPreference(AUTOMATIC_ATTACHMENT_DELETION);
        if (automaticAttachmentDeletionList != null) {
            final int[] choices = getResources().getIntArray(R.array.automatic_message_deletion_values);
            CharSequence[] entries = new CharSequence[choices.length];
            CharSequence[] entryValues = new CharSequence[choices.length];
            for (int i = 0; i < choices.length; ++i) {
                entryValues[i] = String.valueOf(choices[i]);
                if (choices[i] == 0) {
                    entries[i] = getString(R.string.never);
                } else {
                    entries[i] = TimeFrameUtils.resolve(this, 1000L * choices[i]);
                }
            }
            automaticAttachmentDeletionList.setEntries(entries);
            automaticAttachmentDeletionList.setEntryValues(entryValues);
        }

        boolean removeLocation = new Intent("eu.siacs.conversations.location.request").resolveActivity(getPackageManager()) == null;
        boolean removeVoice = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION).resolveActivity(getPackageManager()) == null;

        ListPreference quickAction = (ListPreference) mSettingsFragment.findPreference("quick_action");
        if (quickAction != null && (removeLocation || removeVoice)) {
            ArrayList<CharSequence> entries =
                    new ArrayList<>(Arrays.asList(quickAction.getEntries()));
            ArrayList<CharSequence> entryValues =
                    new ArrayList<>(Arrays.asList(quickAction.getEntryValues()));
            int index = entryValues.indexOf("location");
            if (index > 0 && removeLocation) {
                entries.remove(index);
                entryValues.remove(index);
            }
            index = entryValues.indexOf("voice");
            if (index > 0 && removeVoice) {
                entries.remove(index);
                entryValues.remove(index);
            }
            quickAction.setEntries(entries.toArray(new CharSequence[entries.size()]));
            quickAction.setEntryValues(entryValues.toArray(new CharSequence[entryValues.size()]));
        }

        if (isQuickShareAttachmentChoiceChecked) {
            if (UIPreferenceScreen != null && quickAction != null) {
                UIPreferenceScreen.removePreference(quickAction);
            }
        }

        final Preference removeCertsPreference = mSettingsFragment.findPreference("remove_trusted_certificates");
        if (removeCertsPreference != null) {
            removeCertsPreference.setOnPreferenceClickListener(
                    preference -> {
                        final MemorizingTrustManager mtm =
                                xmppConnectionService.getMemorizingTrustManager();
                final ArrayList<String> aliases = Collections.list(mtm.getCertificates());
                if (aliases.size() == 0) {
                    displayToast(getString(R.string.toast_no_trusted_certs));
                    return true;
                }
                final ArrayList<Integer> selectedItems = new ArrayList<>();
                        final AlertDialog.Builder dialogBuilder =
                                new AlertDialog.Builder(SettingsActivity.this);
                        dialogBuilder.setTitle(
                                getResources().getString(R.string.dialog_manage_certs_title));
                        dialogBuilder.setMultiChoiceItems(
                                aliases.toArray(new CharSequence[aliases.size()]),
                                null,
                        (dialog, indexSelected, isChecked) -> {
                            if (isChecked) {
                                selectedItems.add(indexSelected);
                            } else if (selectedItems.contains(indexSelected)) {
                                selectedItems.remove(Integer.valueOf(indexSelected));
                            }
                            if (selectedItems.size() > 0)
                                ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                            else {
                                ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                            }
                        });

                dialogBuilder.setPositiveButton(
                        getResources()
                                .getString(R.string.dialog_manage_certs_positivebutton),
                        (dialog, which) -> {
                            int count = selectedItems.size();
                            if (count > 0) {
                                for (int i = 0; i < count; i++) {
                                    try {
                                        Integer item =
                                                Integer.valueOf(
                                                        selectedItems.get(i).toString());
                                        String alias = aliases.get(item);
                                        mtm.deleteCertificate(alias);
                                    } catch (KeyStoreException e) {
                                        e.printStackTrace();
                                        displayToast("Error: " + e.getLocalizedMessage());
                                    }
                                }
                                if (xmppConnectionServiceBound) {
                                    reconnectAccounts();
                                }
                                displayToast(
                                        getResources()
                                                .getQuantityString(
                                                        R.plurals.toast_delete_certificates,
                                                        count,
                                                        count));
                            }
                        });
                        dialogBuilder.setNegativeButton(
                                getResources()
                                        .getString(R.string.dialog_manage_certs_negativebutton),
                                null);
                        AlertDialog removeCertsDialog = dialogBuilder.create();
                removeCertsDialog.show();
                removeCertsDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                return true;
            });
            updateTheme();
        }

        final Preference createBackupPreference = mSettingsFragment.findPreference("create_backup");
        if (createBackupPreference != null) {
            createBackupPreference.setSummary(getString(R.string.pref_create_backup_summary, getBackupDirectory(null)));
            createBackupPreference.setOnPreferenceClickListener(preference -> {
                if (hasStoragePermission(REQUEST_CREATE_BACKUP)) {
                    createBackup(true);
                }
                return true;
            });
        }

        final Preference importSettingsPreference = mSettingsFragment.findPreference("import_settings");
        if (importSettingsPreference != null) {
            importSettingsPreference.setSummary(getString(R.string.pref_import_settings_summary));
            importSettingsPreference.setOnPreferenceClickListener(preference -> {
                if (hasStoragePermission(REQUEST_IMPORT_SETTINGS)) {
                    importSettings();
                }
                return true;
            });
        }

        final Preference prefereXmppAvatarPreference = mSettingsFragment.findPreference(PREFER_XMPP_AVATAR);
        if (prefereXmppAvatarPreference != null) {
            prefereXmppAvatarPreference.setOnPreferenceClickListener(preference -> {
                new Thread(() -> xmppConnectionService.getBitmapCache().evictAll()).start();
                return true;
            });
        }

        final Preference showIntroAgainPreference = mSettingsFragment.findPreference("show_intro");
        if (showIntroAgainPreference != null) {
            showIntroAgainPreference.setSummary(getString(R.string.pref_show_intro_summary));
            showIntroAgainPreference.setOnPreferenceClickListener(preference -> {
                showIntroAgain();
                return true;
            });
        }


        final Preference cameraChooserPreference = mSettingsFragment.findPreference(CAMERA_CHOICE);
        if (cameraChooserPreference != null) {
            cameraChooserPreference.setOnPreferenceClickListener(preference -> {
                final List<CameraUtils> cameraApps = CameraUtils.getCameraApps(this);
                showCameraChooser(this, cameraApps);
                return true;
            });
        }

        if (Config.ONLY_INTERNAL_STORAGE) {
            final Preference cleanCachePreference = mSettingsFragment.findPreference("clean_cache");
            if (cleanCachePreference != null) {
                cleanCachePreference.setOnPreferenceClickListener(preference -> cleanCache());
            }

            final Preference cleanPrivateStoragePreference =
                    mSettingsFragment.findPreference("clean_private_storage");
            if (cleanPrivateStoragePreference != null) {
                cleanPrivateStoragePreference.setOnPreferenceClickListener(
                        preference -> cleanPrivateStorage());
            }
        }

        final Preference deleteOmemoPreference =
                mSettingsFragment.findPreference("delete_omemo_identities");
        if (deleteOmemoPreference != null) {
            deleteOmemoPreference.setOnPreferenceClickListener(
                    preference -> deleteOmemoIdentities());
        }
        if (Config.omemoOnly()) {
            final PreferenceScreen securityScreen =
                    (PreferenceScreen) mSettingsFragment.findPreference("security");
            final Preference omemoPreference = mSettingsFragment.findPreference(OMEMO_SETTING);
            if (omemoPreference != null) {
                securityScreen.removePreference(omemoPreference);
            }
        }

        PreferenceScreen ExpertPreferenceScreen = (PreferenceScreen) mSettingsFragment.findPreference("expert");
        final Preference useBundledEmojis = mSettingsFragment.findPreference("use_bundled_emoji");
        if (useBundledEmojis != null) {
            Log.d(Config.LOGTAG, "Bundled Emoji checkbox checked: " + isBundledEmojiChecked);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (isBundledEmojiChecked) {
                    ((CheckBoxPreference) BundledEmojiPreference).setChecked(false);
                    useBundledEmojis.setEnabled(false);
                }
                PreferenceCategory UICatergory = (PreferenceCategory) mSettingsFragment.findPreference("UI");
                UICatergory.removePreference(useBundledEmojis);
                if (UICatergory.getPreferenceCount() == 0) {
                    if (ExpertPreferenceScreen != null) {
                        ExpertPreferenceScreen.removePreference(UICatergory);
                    }
                }
            }
        }

        final Preference enableMultiAccountsPreference = mSettingsFragment.findPreference("enable_multi_accounts");
        if (enableMultiAccountsPreference != null) {
            Log.d(Config.LOGTAG, "Multi account checkbox checked: " + isMultiAccountChecked);
            if (isMultiAccountChecked) {
                enableMultiAccountsPreference.setEnabled(false);
                int accounts = getNumberOfAccounts();
                Log.d(Config.LOGTAG, "Disable multi account: Number of accounts " + accounts);
                if (accounts > 1) {
                    Log.d(Config.LOGTAG, "Disabling multi account not possible because you have more than one account");
                    enableMultiAccountsPreference.setEnabled(false);
                } else {
                    Log.d(Config.LOGTAG, "Disabling multi account possible because you have only one account");
                    enableMultiAccountsPreference.setEnabled(true);
                    enableMultiAccountsPreference.setOnPreferenceClickListener(preference -> {
                        refreshUiReal();
                        return true;
                    });
                }
            } else {
                enableMultiAccountsPreference.setEnabled(true);
                enableMultiAccountsPreference.setOnPreferenceClickListener(preference -> {
                    enableMultiAccounts();
                    return true;
                });
            }
        }

        final Preference removeAllIndividualNotifications = mSettingsFragment.findPreference("remove_all_individual_notifications");
        if (removeAllIndividualNotifications != null) {
            removeAllIndividualNotifications.setOnPreferenceClickListener(preference -> {
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
                dialogBuilder.setTitle(getResources().getString(R.string.remove_individual_notifications));
                dialogBuilder.setMessage(R.string.remove_all_individual_notifications_message);
                dialogBuilder.setPositiveButton(
                        getResources().getString(R.string.yes), (dialog, which) -> {
                            if (Compatibility.runsTwentySix()) {
                                try {
                                    xmppConnectionService.getNotificationService().cleanAllNotificationChannels(SettingsActivity.this);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                xmppConnectionService.updateNotificationChannels();
                            }
                        });
                dialogBuilder.setNegativeButton(getResources().getString(R.string.no), null);
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
                return true;
            });
            updateTheme();
        }
    }

    private void updateTheme() {
        final int theme = findTheme();
        if (this.mTheme != theme) {
            refreshUiReal();
        }
    }

    private void changeOmemoSettingSummary() {
        final ListPreference omemoPreference =
                (ListPreference) mSettingsFragment.findPreference(OMEMO_SETTING);
        if (omemoPreference == null) {
            return;
        }
        final String value = omemoPreference.getValue();
        switch (value) {
            case "always":
                omemoPreference.setSummary(R.string.pref_omemo_setting_summary_always);
                break;
            case "default_on":
                omemoPreference.setSummary(R.string.pref_omemo_setting_summary_default_on);
                break;
            case "default_off":
                omemoPreference.setSummary(R.string.pref_omemo_setting_summary_default_off);
                break;
                case "always_off":
                    omemoPreference.setSummary(R.string.pref_omemo_setting_summary_always_off);
                    break;
        }
    }

    private boolean isCallable(final Intent i) {
        return i != null
                && getPackageManager()
                .queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY)
                .size()
                > 0;
    }

    private boolean cleanCache() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
        return true;
    }

    private boolean cleanPrivateStorage() {
        for (String type : Arrays.asList("Images", "Videos", "Files", "Audios")) {
            cleanPrivateFiles(type);
        }
        return true;
    }

    private void cleanPrivateFiles(final String type) {
        try {
            File dir = new File(getFilesDir().getAbsolutePath(), File.separator + type + File.separator);
            File[] array = dir.listFiles();
            if (array != null) {
                for (int b = 0; b < array.length; b++) {
                    String name = array[b].getName().toLowerCase();
                    if (name.equals(".nomedia")) {
                        continue;
                    }
                    if (array[b].isFile()) {
                        array[b].delete();
                    }
                }
            }
        } catch (Throwable e) {
            Log.e("CleanCache", e.toString());
        }
    }

    private boolean deleteOmemoIdentities() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pref_delete_omemo_identities);
        final List<CharSequence> accounts = new ArrayList<>();
        for (Account account : xmppConnectionService.getAccounts()) {
            if (account.isEnabled()) {
                accounts.add(account.getJid().asBareJid().toString());
            }
        }
        final boolean[] checkedItems = new boolean[accounts.size()];
        builder.setMultiChoiceItems(
                accounts.toArray(new CharSequence[accounts.size()]),
                checkedItems,
                (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
            final AlertDialog alertDialog = (AlertDialog) dialog;
            for (boolean item : checkedItems) {
                if (item) {
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                    return;
                }
            }
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(
                R.string.delete_selected_keys,
                (dialog, which) -> {
                    for (int i = 0; i < checkedItems.length; ++i) {
                if (checkedItems[i]) {
                    try {
                        Jid jid = Jid.of(accounts.get(i).toString());
                        Account account = xmppConnectionService.findAccountByJid(jid);
                        if (account != null) {
                            account.getAxolotlService().regenerateKeys(true);
                        }
                    } catch (IllegalArgumentException e) {
                        //
                    }
                }
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        return true;
    }

    private void enableMultiAccounts() {
        if (!isMultiAccountChecked) {
            multiAccountPreference.setEnabled(true);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String name) {
        final List<String> resendPresence = Arrays.asList(
                CONFIRM_MESSAGES,
                DND_ON_SILENT_MODE,
                AWAY_WHEN_SCREEN_IS_OFF,
                ALLOW_MESSAGE_CORRECTION,
                TREAT_VIBRATE_AS_SILENT,
                MANUALLY_CHANGE_PRESENCE,
                BROADCAST_LAST_ACTIVITY);
        FileBackend.switchStorage(preferences.getBoolean(USE_INNER_STORAGE, true));
        if (name.equals(OMEMO_SETTING)) {
            OmemoSetting.load(this, preferences);
            changeOmemoSettingSummary();
        } else if (name.equals(SHOW_FOREGROUND_SERVICE)) {
            xmppConnectionService.toggleForegroundService();
        } else if (resendPresence.contains(name)) {
            if (xmppConnectionServiceBound) {
                if (name.equals(AWAY_WHEN_SCREEN_IS_OFF)
                        || name.equals(MANUALLY_CHANGE_PRESENCE)) {
                    xmppConnectionService.toggleScreenEventReceiver();
                }
                xmppConnectionService.refreshAllPresences();
            }
        } else if (name.equals("dont_trust_system_cas")) {
            xmppConnectionService.updateMemorizingTrustmanager();
            reconnectAccounts();
        } else if (name.equals("use_tor")) {
            if (preferences.getBoolean(name, false)) {
                displayToast(getString(R.string.audio_video_disabled_tor));
            }
            reconnectAccounts();
            xmppConnectionService.reinitializeMuclumbusService();
        } else if (name.equals(AUTOMATIC_MESSAGE_DELETION)) {
            xmppConnectionService.expireOldMessages(true);
        } else if (name.equals(THEME) || name.equals(THEME_COLOR)) {
            updateTheme();
        } else if (name.equals(USE_UNICOLORED_CHATBG)) {
            xmppConnectionService.updateConversationUi();
        }
        else if (UnifiedPushDistributor.PREFERENCES.contains(name)) {
            if (xmppConnectionService.reconfigurePushDistributor()) {
                xmppConnectionService.renewUnifiedPushEndpoints();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (requestCode == REQUEST_CREATE_BACKUP) {
                    createBackup(true);
                }
                if (requestCode == REQUEST_IMPORT_SETTINGS) {
                    importSettings();
                }
            } else {
                ToastCompat.makeText(
                        this,

                        R.string.no_storage_permission,
                        ToastCompat.LENGTH_SHORT).show();
            }
        }
    }

    private void createBackup(boolean notify) {
        final Intent intent = new Intent(this, ExportBackupService.class);
        intent.putExtra("NOTIFY_ON_BACKUP_COMPLETE", notify);
        ContextCompat.startForegroundService(this, intent);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.backup_started_message);
        builder.setPositiveButton(R.string.ok, null);
        builder.create().show();
    }

    @SuppressWarnings({ "unchecked" })
    private boolean importSettings() {
        boolean success;
        ObjectInputStream input = null;
        try {
            final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + APP_DIRECTORY + File.separator + "Database" + File.separator, "settings.dat");
            input = new ObjectInputStream(new FileInputStream(file));
            SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
            prefEdit.clear();
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object value = entry.getValue();
                String key = entry.getKey();

                if (value instanceof Boolean)
                    prefEdit.putBoolean(key, ((Boolean) value).booleanValue());
                else if (value instanceof Float)
                    prefEdit.putFloat(key, ((Float) value).floatValue());
                else if (value instanceof Integer)
                    prefEdit.putInt(key, ((Integer) value).intValue());
                else if (value instanceof Long)
                    prefEdit.putLong(key, ((Long) value).longValue());
                else if (value instanceof String)
                    prefEdit.putString(key, ((String) value));
            }
            prefEdit.commit();
            success = true;
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (success) {
            ToastCompat.makeText(this, R.string.success_import_settings, ToastCompat.LENGTH_SHORT).show();
        } else {
            ToastCompat.makeText(this, R.string.error_import_settings, ToastCompat.LENGTH_SHORT).show();
        }
        return success;
    }

    private void displayToast(final String msg) {
        runOnUiThread(() -> ToastCompat.makeText(SettingsActivity.this, msg, ToastCompat.LENGTH_LONG).show());
    }

    private void reconnectAccounts() {
        for (Account account : xmppConnectionService.getAccounts()) {
            if (account.isEnabled()) {
                xmppConnectionService.reconnectAccountInBackground(account);
            }
        }
    }

    public void refreshUiReal() {
        recreate();
    }

    private int getNumberOfAccounts() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int NumberOfAccounts = preferences.getInt(NUMBER_OF_ACCOUNTS, 0);
        Log.d(Config.LOGTAG, "Get number of accounts from file: " + NumberOfAccounts);
        return NumberOfAccounts;
    }

    private void showIntroAgain() {
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
        Map<String, ?> allEntries = getPrefs.getAll();
        int success = -1;
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().contains("intro_shown_on_activity")) {
                SharedPreferences.Editor e = getPrefs.edit();
                e.putBoolean(entry.getKey(), true);
                if (e.commit()) {
                    if (success != 0) {
                        success = 1;
                    }
                } else {
                    success = 0;
                }
            }
        }
        if (success == 1) {
            ToastCompat.makeText(this, R.string.show_intro_again, ToastCompat.LENGTH_SHORT).show();
        } else {
            ToastCompat.makeText(this, R.string.show_intro_again_failed, ToastCompat.LENGTH_SHORT).show();
        }
    }
}
