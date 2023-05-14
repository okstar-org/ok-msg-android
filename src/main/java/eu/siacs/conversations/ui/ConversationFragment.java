package eu.siacs.conversations.ui;

import java.util.Map;
import eu.siacs.conversations.ui.adapter.CommandAdapter;
import eu.siacs.conversations.xml.Element;
import eu.siacs.conversations.xmpp.stanzas.IqPacket;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static eu.siacs.conversations.ui.SettingsActivity.HIDE_YOU_ARE_NOT_PARTICIPATING;
import static eu.siacs.conversations.ui.SettingsActivity.WARN_UNENCRYPTED_CHAT;
import static eu.siacs.conversations.ui.XmppActivity.EXTRA_ACCOUNT;
import static eu.siacs.conversations.ui.XmppActivity.REQUEST_INVITE_TO_CONVERSATION;
import static eu.siacs.conversations.ui.util.SoftKeyboardUtils.hideSoftKeyboard;
import static eu.siacs.conversations.utils.PermissionUtils.allGranted;
import static eu.siacs.conversations.utils.PermissionUtils.getFirstDenied;
import static eu.siacs.conversations.utils.PermissionUtils.readGranted;
import static eu.siacs.conversations.utils.StorageHelper.getConversationsDirectory;
import static eu.siacs.conversations.xmpp.Patches.ENCRYPTION_EXCEPTIONS;
import com.google.common.collect.ImmutableList;
import static eu.siacs.conversations.utils.CameraUtils.getCameraApp;
import static eu.siacs.conversations.utils.CameraUtils.showCameraChooser;
import eu.siacs.conversations.utils.PermissionUtils;
import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import eu.siacs.conversations.utils.Emoticons;


import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;
import androidx.databinding.DataBindingUtil;
import android.text.SpannableStringBuilder;

import com.google.common.base.Optional;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import eu.siacs.conversations.utils.TimeFrameUtils;
import eu.siacs.conversations.xml.Element;
import eu.siacs.conversations.xmpp.jingle.AbstractJingleConnection;
import eu.siacs.conversations.xmpp.jingle.JingleConnectionManager;
import eu.siacs.conversations.xmpp.jingle.Media;
import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;
import eu.siacs.conversations.crypto.axolotl.AxolotlService;
import eu.siacs.conversations.crypto.axolotl.FingerprintStatus;
import eu.siacs.conversations.databinding.FragmentConversationBinding;
import eu.siacs.conversations.entities.Account;
import eu.siacs.conversations.entities.Blockable;
import eu.siacs.conversations.entities.Contact;
import eu.siacs.conversations.entities.Conversation;
import eu.siacs.conversations.entities.Conversational;
import eu.siacs.conversations.entities.DownloadableFile;
import eu.siacs.conversations.entities.Edit;
import eu.siacs.conversations.entities.Message;
import eu.siacs.conversations.entities.MucOptions;
import eu.siacs.conversations.entities.MucOptions.User;
import eu.siacs.conversations.entities.Presence;
import eu.siacs.conversations.entities.ReadByMarker;
import eu.siacs.conversations.entities.Transferable;
import eu.siacs.conversations.entities.TransferablePlaceholder;
import eu.siacs.conversations.http.HttpDownloadConnection;
import eu.siacs.conversations.persistance.FileBackend;
import eu.siacs.conversations.services.AttachFileToConversationRunnable;
import eu.siacs.conversations.services.MessageArchiveService;
import eu.siacs.conversations.services.XmppConnectionService;
import eu.siacs.conversations.ui.adapter.MediaPreviewAdapter;
import eu.siacs.conversations.ui.adapter.MessageAdapter;
import eu.siacs.conversations.ui.adapter.MessageLogAdapter;
import eu.siacs.conversations.ui.adapter.model.MessageLogModel;
import eu.siacs.conversations.ui.util.ActivityResult;
import eu.siacs.conversations.ui.util.Attachment;
import eu.siacs.conversations.ui.util.CallManager;
import eu.siacs.conversations.ui.util.ConversationMenuConfigurator;
import eu.siacs.conversations.ui.util.DateSeparator;
import eu.siacs.conversations.ui.util.EditMessageActionModeCallback;
import eu.siacs.conversations.ui.util.KeyboardUtils;
import eu.siacs.conversations.ui.util.ListViewUtils;
import eu.siacs.conversations.ui.util.MucDetailsContextMenuHelper;
import eu.siacs.conversations.ui.util.PendingItem;
import eu.siacs.conversations.ui.util.PresenceSelector;
import eu.siacs.conversations.ui.util.QuoteHelper;
import eu.siacs.conversations.ui.util.ScrollState;
import eu.siacs.conversations.ui.util.SendButtonAction;
import eu.siacs.conversations.ui.util.SendButtonTool;
import eu.siacs.conversations.ui.util.ShareUtil;
import eu.siacs.conversations.ui.util.StyledAttributes;
import eu.siacs.conversations.ui.util.ViewUtil;
import eu.siacs.conversations.ui.widget.EditMessage;
import eu.siacs.conversations.utils.CameraUtils;
import eu.siacs.conversations.utils.Compatibility;
import eu.siacs.conversations.utils.GeoHelper;
import eu.siacs.conversations.utils.MenuDoubleTabUtil;
import eu.siacs.conversations.utils.MessageUtils;
import eu.siacs.conversations.utils.MimeUtils;
import eu.siacs.conversations.xml.Namespace;
import eu.siacs.conversations.utils.NickValidityChecker;
import eu.siacs.conversations.utils.Patterns;
import eu.siacs.conversations.utils.QuickLoader;
import eu.siacs.conversations.utils.StylingHelper;
import eu.siacs.conversations.utils.UIHelper;
import eu.siacs.conversations.xmpp.Jid;
import eu.siacs.conversations.xmpp.XmppConnection;
import eu.siacs.conversations.xmpp.chatstate.ChatState;
import eu.siacs.conversations.xmpp.jingle.JingleFileTransferConnection;
import eu.siacs.conversations.xmpp.jingle.OngoingRtpSession;
import eu.siacs.conversations.xmpp.jingle.RtpCapability;
import me.drakeet.support.toast.ToastCompat;
import net.java.otr4j.session.SessionStatus;

public class ConversationFragment extends XmppFragment implements EditMessage.KeyboardListener, MessageAdapter.OnContactPictureLongClicked, MessageAdapter.OnContactPictureClicked {

    public static final int REQUEST_SEND_MESSAGE = 0x0201;
    public static final int REQUEST_DECRYPT_PGP = 0x0202;
    public static final int REQUEST_ENCRYPT_MESSAGE = 0x0207;
    public static final int REQUEST_TRUST_KEYS_TEXT = 0x0208;
    public static final int REQUEST_TRUST_KEYS_ATTACHMENTS = 0x0209;
    public static final int REQUEST_START_DOWNLOAD = 0x0210;
    public static final int REQUEST_ADD_EDITOR_CONTENT = 0x0211;
    public static final int REQUEST_COMMIT_ATTACHMENTS = 0x0212;
    public static final int ATTACHMENT_CHOICE = 0x0300;
    public static final int REQUEST_START_AUDIO_CALL = 0x213;
    public static final int REQUEST_START_VIDEO_CALL = 0x214;
    public static final int ATTACHMENT_CHOICE_CHOOSE_IMAGE = 0x0301;
    public static final int ATTACHMENT_CHOICE_TAKE_PHOTO = 0x0302;
    public static final int ATTACHMENT_CHOICE_CHOOSE_FILE = 0x0303;
    public static final int ATTACHMENT_CHOICE_RECORD_VOICE = 0x0304;
    public static final int ATTACHMENT_CHOICE_LOCATION = 0x0305;
    public static final int ATTACHMENT_CHOICE_CHOOSE_VIDEO = 0x0306;
    public static final int ATTACHMENT_CHOICE_RECORD_VIDEO = 0x0307;
    public static final int ATTACHMENT_CHOICE_INVALID = 0x0399;

    public static final String RECENTLY_USED_QUICK_ACTION = "recently_used_quick_action";
    public static final String STATE_CONVERSATION_UUID = ConversationFragment.class.getName() + ".uuid";
    public static final String STATE_SCROLL_POSITION = ConversationFragment.class.getName() + ".scroll_position";
    public static final String STATE_PHOTO_URI = ConversationFragment.class.getName() + ".media_previews";
    public static final String STATE_MEDIA_PREVIEWS = ConversationFragment.class.getName() + ".take_photo_uri";

    private static final String STATE_LAST_MESSAGE_UUID = "state_last_message_uuid";

    private final List<Message> messageList = new ArrayList<>();
    private final PendingItem<ActivityResult> postponedActivityResult = new PendingItem<>();
    private final PendingItem<String> pendingConversationsUuid = new PendingItem<>();
    private final PendingItem<ArrayList<Attachment>> pendingMediaPreviews = new PendingItem<>();
    private final PendingItem<Bundle> pendingExtras = new PendingItem<>();
    private final PendingItem<Uri> pendingTakePhotoUri = new PendingItem<>();
    private final PendingItem<Uri> pendingTakeVideoUri = new PendingItem<>();
    private final PendingItem<ScrollState> pendingScrollState = new PendingItem<>();
    private final PendingItem<String> pendingLastMessageUuid = new PendingItem<>();
    private final PendingItem<Message> pendingMessage = new PendingItem<>();
    public Uri mPendingEditorContent = null;
    public FragmentConversationBinding binding;
    protected MessageAdapter messageListAdapter;
    protected CommandAdapter commandAdapter;
    private String lastMessageUuid = null;
    private Conversation conversation;
    private Toast messageLoaderToast;
    private ConversationsActivity activity;
    private Menu mOptionsMenu;
    protected OnClickListener clickToVerify = new OnClickListener() {
        @Override
        public void onClick(View v) {
            activity.verifyOtrSessionDialog(conversation, v);
        }
    };

    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd  hh:mm (z)", Locale.US);

    private boolean reInitRequiredOnStart = true;
    private MediaPreviewAdapter mediaPreviewAdapter;
    private final OnClickListener clickToMuc = new OnClickListener() {

        @Override
        public void onClick(View v) {
            ConferenceDetailsActivity.open(getActivity(), conversation);
        }
    };
    private final OnClickListener leaveMuc = new OnClickListener() {

        @Override
        public void onClick(View v) {
            activity.xmppConnectionService.archiveConversation(conversation);
        }
    };
    private final OnClickListener joinMuc = new OnClickListener() {

        @Override
        public void onClick(View v) {
            activity.xmppConnectionService.joinMuc(conversation);
        }
    };

    private final OnClickListener acceptJoin = new OnClickListener() {
        @Override
        public void onClick(View v) {
            conversation.setAttribute("accept_non_anonymous", true);
            activity.xmppConnectionService.updateConversation(conversation);
            activity.xmppConnectionService.joinMuc(conversation);
        }
    };

    private final OnClickListener enterPassword = new OnClickListener() {

        @Override
        public void onClick(View v) {
            MucOptions muc = conversation.getMucOptions();
            String password = muc.getPassword();
            if (password == null) {
                password = "";
            }
            activity.quickPasswordEdit(password, value -> {
                activity.xmppConnectionService.providePasswordForMuc(conversation, value);
                return null;
            });
        }
    };

    private final OnClickListener meCommand = v -> Objects.requireNonNull(binding.textinput.getText()).insert(0, Message.ME_COMMAND + " ");
    private final OnClickListener quote = v -> insertQuote();
    private final OnClickListener boldText = v -> insertFormatting("bold");
    private final OnClickListener italicText = v -> insertFormatting("italic");
    private final OnClickListener monospaceText = v -> insertFormatting("monospace");
    private final OnClickListener strikethroughText = v -> insertFormatting("strikethrough");
    private final OnClickListener help = v -> openHelp();
    private final OnClickListener close = v -> closeFormatting();

    private void openHelp() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.format_text);
        builder.setMessage(R.string.help_format_text);
        builder.setNeutralButton(getString(R.string.ok), null);
        builder.create().show();
    }

    private void closeFormatting() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.close);
        builder.setMessage(R.string.close_format_text);
        builder.setPositiveButton(getString(R.string.close),
                (dialog, which) -> {
                    final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
                    preferences.edit().putBoolean("showtextformatting", false).apply();
                    updateSendButton();
                });
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.create().show();
    }

    private void insertFormatting(String format) {
        final String BOLD = "*";
        final String ITALIC = "_";
        final String MONOSPACE = "`";
        final String STRIKETHROUGH = "~";

        int selStart = this.binding.textinput.getSelectionStart();
        int selEnd = this.binding.textinput.getSelectionEnd();
        int min = 0;
        int max = this.binding.textinput.getText().length();
        if (this.binding.textinput.isFocused()) {
            selStart = this.binding.textinput.getSelectionStart();
            selEnd = this.binding.textinput.getSelectionEnd();
            min = Math.max(0, Math.min(selStart, selEnd));
            max = Math.max(0, Math.max(selStart, selEnd));
        }
        final CharSequence selectedText = this.binding.textinput.getText().subSequence(min, max);

        switch (format) {
            case "bold":
                if (selectedText.length() != 0) {
                    this.binding.textinput.getText().replace(Math.min(selStart, selEnd), Math.max(selStart, selEnd),
                            BOLD + selectedText + BOLD, 0, selectedText.length() + 2);
                } else {
                    this.binding.textinput.getText().insert(this.binding.textinput.getSelectionStart(), (BOLD));
                }
                return;
            case "italic":
                if (selectedText.length() != 0) {
                    this.binding.textinput.getText().replace(Math.min(selStart, selEnd), Math.max(selStart, selEnd),
                            ITALIC + selectedText + ITALIC, 0, selectedText.length() + 2);
                } else {
                    this.binding.textinput.getText().insert(this.binding.textinput.getSelectionStart(), (ITALIC));
                }
                return;
            case "monospace":
                if (selectedText.length() != 0) {
                    this.binding.textinput.getText().replace(Math.min(selStart, selEnd), Math.max(selStart, selEnd),
                            MONOSPACE + selectedText + MONOSPACE, 0, selectedText.length() + 2);
                } else {
                    this.binding.textinput.getText().insert(this.binding.textinput.getSelectionStart(), (MONOSPACE));
                }
                return;
            case "strikethrough":
                if (selectedText.length() != 0) {
                    this.binding.textinput.getText().replace(Math.min(selStart, selEnd), Math.max(selStart, selEnd),
                            STRIKETHROUGH + selectedText + STRIKETHROUGH, 0, selectedText.length() + 2);
                } else {
                    this.binding.textinput.getText().insert(this.binding.textinput.getSelectionStart(), (STRIKETHROUGH));
                }
                return;
        }
    }
    private void insertQuote() {
        int pos = 0;
        if (this.binding.textinput.getSelectionStart() == this.binding.textinput.getSelectionEnd()) {
            pos = this.binding.textinput.getSelectionStart();
        }
        if (pos == 0) {
            Objects.requireNonNull(binding.textinput.getText()).insert(0, QuoteHelper.QUOTE_CHAR + " ");
        } else {
            Objects.requireNonNull(binding.textinput.getText()).insert(pos, System.getProperty("line.separator") + QuoteHelper.QUOTE_CHAR + " ");
        }
    }

    private final OnScrollListener mOnScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (AbsListView.OnScrollListener.SCROLL_STATE_IDLE == scrollState) {
                fireReadEvent();
            }
        }

        @Override
        public void onScroll(final AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            toggleScrollDownButton(view);
            synchronized (ConversationFragment.this.messageList) {
                if (firstVisibleItem < 5 && conversation != null && conversation.messagesLoaded.compareAndSet(true, false) && messageList.size() > 0) {
                    long timestamp;
                    if (messageList.get(0).getType() == Message.TYPE_STATUS && messageList.size() >= 2) {
                        timestamp = messageList.get(1).getTimeSent();
                    } else {
                        timestamp = messageList.get(0).getTimeSent();
                    }
                    activity.xmppConnectionService.loadMoreMessages(conversation, timestamp, new XmppConnectionService.OnMoreMessagesLoaded() {
                        @Override
                        public void onMoreMessagesLoaded(final int c, final Conversation conversation) {
                            if (ConversationFragment.this.conversation != conversation) {
                                conversation.messagesLoaded.set(true);
                                return;
                            }
                            runOnUiThread(() -> {
                                synchronized (messageList) {
                                    final int oldPosition = binding.messagesView.getFirstVisiblePosition();
                                    Message message = null;
                                    int childPos;
                                    for (childPos = 0; childPos + oldPosition < messageList.size(); ++childPos) {
                                        message = messageList.get(oldPosition + childPos);
                                        if (message.getType() != Message.TYPE_STATUS) {
                                            break;
                                        }
                                    }
                                    final String uuid = message != null ? message.getUuid() : null;
                                    View v = binding.messagesView.getChildAt(childPos);
                                    final int pxOffset = (v == null) ? 0 : v.getTop();
                                    ConversationFragment.this.conversation.populateWithMessages(ConversationFragment.this.messageList);
                                    try {
                                        updateStatusMessages();
                                    } catch (IllegalStateException e) {
                                        Log.d(Config.LOGTAG, "caught illegal state exception while updating status messages");
                                    }
                                    messageListAdapter.notifyDataSetChanged();
                                    int pos = Math.max(getIndexOf(uuid, messageList), 0);
                                    binding.messagesView.setSelectionFromTop(pos, pxOffset);
                                    if (messageLoaderToast != null) {
                                        messageLoaderToast.cancel();
                                    }
                                    conversation.messagesLoaded.set(true);
                                }
                            });
                        }

                        @Override
                        public void informUser(final int resId) {

                            runOnUiThread(() -> {
                                if (messageLoaderToast != null) {
                                    messageLoaderToast.cancel();
                                }
                                if (ConversationFragment.this.conversation != conversation) {
                                    return;
                                }
                                messageLoaderToast = ToastCompat.makeText(view.getContext(), resId, ToastCompat.LENGTH_LONG);
                                messageLoaderToast.show();
                            });
                        }
                    });
                }
            }
        }
    };
    private final EditMessage.OnCommitContentListener mEditorContentListener = new EditMessage.OnCommitContentListener() {
        @Override
        public boolean onCommitContent(InputContentInfoCompat inputContentInfo, int flags, Bundle opts, String[] contentMimeTypes) {
            // try to get permission to read the image, if applicable
            if ((flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
                try {
                    inputContentInfo.requestPermission();
                } catch (Exception e) {
                    Log.e(Config.LOGTAG, "InputContentInfoCompat#requestPermission() failed.", e);
                    ToastCompat.makeText(getActivity(), activity.getString(R.string.no_permission_to_access_x, inputContentInfo.getDescription()), ToastCompat.LENGTH_LONG
                    ).show();
                    return false;
                }
            }
            if (hasPermissions(REQUEST_ADD_EDITOR_CONTENT, Manifest.permission.WRITE_EXTERNAL_STORAGE) && hasPermissions(REQUEST_ADD_EDITOR_CONTENT, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                attachEditorContentToConversation(inputContentInfo.getContentUri());
            } else {
                mPendingEditorContent = inputContentInfo.getContentUri();
            }
            return true;
        }
    };
    private Message selectedMessage;
    private final OnClickListener mEnableAccountListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final Account account = conversation == null ? null : conversation.getAccount();
            if (account != null) {
                account.setOption(Account.OPTION_DISABLED, false);
                activity.xmppConnectionService.updateAccount(account);
            }
        }
    };
    private final OnClickListener mUnblockClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            v.post(() -> v.setVisibility(View.INVISIBLE));
            if (conversation.isDomainBlocked()) {
                BlockContactDialog.show(activity, conversation);
            } else {
                unblockConversation(conversation);
            }
        }
    };
    private final OnClickListener mBlockClickListener = this::showBlockSubmenu;

    private final OnClickListener mAddBackClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            final Contact contact = conversation == null ? null : conversation.getContact();
            if (contact != null) {
                activity.xmppConnectionService.createContact(contact, true);
                activity.switchToContactDetails(contact);
            }
        }
    };

    private final View.OnLongClickListener mLongPressBlockListener = this::showBlockSubmenu;

    private final OnClickListener mHideUnencryptionHint = v -> enableMessageEncryption();

    private void enableMessageEncryption() {
        if (Config.supportOmemo() && Conversation.suitableForOmemoByDefault(conversation) && conversation.isSingleOrPrivateAndNonAnonymous()) {
            conversation.setNextEncryption(Message.ENCRYPTION_AXOLOTL);
            activity.xmppConnectionService.updateConversation(conversation);
            activity.refreshUi();
        }
        hideSnackbar();
    }
    private void disableMessageEncryption() {
        if (conversation.isSingleOrPrivateAndNonAnonymous()) {
            conversation.setNextEncryption(Message.ENCRYPTION_NONE);
            activity.xmppConnectionService.updateConversation(conversation);
            activity.refreshUi();
        }
        hideSnackbar();
    }



    private final OnClickListener mAllowPresenceSubscription = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final Contact contact = conversation == null ? null : conversation.getContact();
            if (contact != null) {
                activity.xmppConnectionService.sendPresencePacket(contact.getAccount(),
                        activity.xmppConnectionService.getPresenceGenerator()
                                .sendPresenceUpdatesTo(contact));
                hideSnackbar();
            }
        }
    };
    private OnClickListener mAnswerSmpClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(activity, VerifyOTRActivity.class);
            intent.setAction(VerifyOTRActivity.ACTION_VERIFY_CONTACT);
            intent.putExtra(EXTRA_ACCOUNT, conversation.getAccount().getJid().asBareJid().toString());
            intent.putExtra(VerifyOTRActivity.EXTRA_ACCOUNT, conversation.getAccount().getJid().asBareJid().toString());
            intent.putExtra("mode", VerifyOTRActivity.MODE_ANSWER_QUESTION);
            startActivity(intent);
            activity.overridePendingTransition(R.animator.fade_in, R.animator.fade_out);
        }
    };

    protected OnClickListener clickToDecryptListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            PendingIntent pendingIntent = conversation.getAccount().getPgpDecryptionService().getPendingIntent();
            if (pendingIntent != null) {
                try {
                    getActivity().startIntentSenderForResult(pendingIntent.getIntentSender(),
                            REQUEST_DECRYPT_PGP,
                            null,
                            0,
                            0,
                            0);
                } catch (SendIntentException e) {
                    ToastCompat.makeText(getActivity(), R.string.unable_to_connect_to_keychain, ToastCompat.LENGTH_SHORT).show();
                    conversation.getAccount().getPgpDecryptionService().continueDecryption(true);
                }
            }
            updateSnackBar(conversation);
        }
    };
    private final AtomicBoolean mSendingPgpMessage = new AtomicBoolean(false);
    private final OnEditorActionListener mEditorActionListener = (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && imm.isFullscreenMode()) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            sendMessage();
            return true;
        } else {
            return false;
        }
    };
    private final OnClickListener mScrollButtonListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            stopScrolling();
            setSelection(binding.messagesView.getCount() - 1, true);
        }
    };

    private final OnClickListener mRecordVoiceButtonListener = v -> attachFile(ATTACHMENT_CHOICE_RECORD_VOICE);

    private final OnClickListener mSendButtonListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Object tag = v.getTag();
            if (tag instanceof SendButtonAction) {
                SendButtonAction action = (SendButtonAction) tag;
                switch (action) {
                    case CHOOSE_ATTACHMENT:
                        choose_attachment(v);
                    case TAKE_PHOTO:
                    case RECORD_VIDEO:
                    case SEND_LOCATION:
                    case RECORD_VOICE:
                    case CHOOSE_PICTURE:
                        attachFile(action.toChoice());
                        break;
                    case CANCEL:
                        if (conversation != null) {
                            if (conversation.setCorrectingMessage(null)) {
                                binding.textinput.setText("");
                                binding.textinput.append(conversation.getDraftMessage());
                                conversation.setDraftMessage(null);
                            } else if (conversation.getMode() == Conversation.MODE_MULTI) {
                                conversation.setNextCounterpart(null);
                                binding.textinput.setText("");
                            } else {
                                binding.textinput.setText("");
                            }
                            updateChatMsgHint();
                            updateSendButton();
                            updateEditablity();
                        }
                        break;
                    default:
                        sendMessage();
                }
            } else {
                sendMessage();
            }
        }
    };

    private View.OnLongClickListener mSendButtonLongListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            final String body = binding.textinput.getText().toString();
            if (body.length() == 0) {
                binding.textinput.getText().insert(0, Message.ME_COMMAND + " ");
            }
            return true;
        }
    };

    private int completionIndex = 0;
    private int lastCompletionLength = 0;
    private String incomplete;
    private int lastCompletionCursor;
    private boolean firstWord = false;
    private Message mPendingDownloadableMessage;

    private static ConversationFragment findConversationFragment(Activity activity) {
        Fragment fragment = activity.getFragmentManager().findFragmentById(R.id.main_fragment);
        if (fragment instanceof ConversationFragment) {
            return (ConversationFragment) fragment;
        }
        fragment = activity.getFragmentManager().findFragmentById(R.id.secondary_fragment);
        if (fragment instanceof ConversationFragment) {
            return (ConversationFragment) fragment;
        }
        return null;
    }

    public static void startStopPending(Activity activity) {
        ConversationFragment fragment = findConversationFragment(activity);
        if (fragment != null) {
            fragment.messageListAdapter.startStopPending();
        }
    }

    public static void downloadFile(Activity activity, Message message) {
        ConversationFragment fragment = findConversationFragment(activity);
        if (fragment != null) {
            fragment.startDownloadable(message);
        }
    }

    public static void registerPendingMessage(Activity activity, Message message) {
        ConversationFragment fragment = findConversationFragment(activity);
        if (fragment != null) {
            fragment.pendingMessage.push(message);
        }
    }

    public static void openPendingMessage(Activity activity) {
        ConversationFragment fragment = findConversationFragment(activity);
        if (fragment != null) {
            Message message = fragment.pendingMessage.pop();
            if (message != null) {
                fragment.messageListAdapter.openDownloadable(message);
            }
        }
    }

    public static Conversation getConversation(Activity activity) {
        return getConversation(activity, R.id.secondary_fragment);
    }

    private static Conversation getConversation(Activity activity, @IdRes int res) {
        final Fragment fragment = activity.getFragmentManager().findFragmentById(res);
        if (fragment instanceof ConversationFragment) {
            return ((ConversationFragment) fragment).getConversation();
        } else {
            return null;
        }
    }

    public static ConversationFragment get(Activity activity) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.main_fragment);
        if (fragment instanceof ConversationFragment) {
            return (ConversationFragment) fragment;
        } else {
            fragment = fragmentManager.findFragmentById(R.id.secondary_fragment);
            return fragment instanceof ConversationFragment ? (ConversationFragment) fragment : null;
        }
    }

    public static Conversation getConversationReliable(Activity activity) {
        final Conversation conversation = getConversation(activity, R.id.secondary_fragment);
        if (conversation != null) {
            return conversation;
        }
        return getConversation(activity, R.id.main_fragment);
    }

    private static boolean scrolledToBottom(AbsListView listView) {
        final int count = listView.getCount();
        if (count == 0) {
            return true;
        } else if (listView.getLastVisiblePosition() == count - 1) {
            final View lastChild = listView.getChildAt(listView.getChildCount() - 1);
            return lastChild != null && lastChild.getBottom() <= listView.getHeight();
        } else {
            return false;
        }
    }

    @SuppressLint("RestrictedApi")
    private void choose_attachment(View v) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(activity);
        final boolean hideVoice = p.getBoolean("show_record_voice_btn", activity.getResources().getBoolean(R.bool.show_record_voice_btn));
        PopupMenu popup = new PopupMenu(activity, v);
        popup.inflate(R.menu.choose_attachment);
        final Menu menu = popup.getMenu();
        ConversationMenuConfigurator.configureQuickShareAttachmentMenu(conversation, menu, hideVoice);
        popup.setOnMenuItemClickListener(attachmentItem -> {
            switch (attachmentItem.getItemId()) {
                case R.id.attach_choose_picture:
                case R.id.attach_choose_video:
                case R.id.attach_take_picture:
                case R.id.attach_record_video:
                case R.id.attach_choose_file:
                case R.id.attach_record_voice:
                case R.id.attach_location:
                    handleAttachmentSelection(attachmentItem);
                default:
                    return false;
            }
        });
        MenuPopupHelper menuHelper = new MenuPopupHelper(getActivity(), (MenuBuilder) menu, v);
        menuHelper.setForceShowIcon(true);
        menuHelper.show();
    }

    private void toggleScrollDownButton() {
        toggleScrollDownButton(binding.messagesView);
    }

    private void toggleScrollDownButton(AbsListView listView) {
        if (conversation == null) {
            return;
        }
        if (scrolledToBottom(listView)) {
            lastMessageUuid = null;
            hideUnreadMessagesCount();
        } else {
            binding.scrollToBottomButton.setEnabled(true);
            binding.scrollToBottomButton.show();
            if (lastMessageUuid == null) {
                lastMessageUuid = conversation.getLatestMessage().getUuid();
            }
            if (conversation.getReceivedMessagesCountSinceUuid(lastMessageUuid) > 0) {
                binding.unreadCountCustomView.setVisibility(View.VISIBLE);
            }
        }
    }

    private int getIndexOf(String uuid, List<Message> messages) {
        if (uuid == null) {
            return messages.size() - 1;
        }
        for (int i = 0; i < messages.size(); ++i) {
            if (uuid.equals(messages.get(i).getUuid())) {
                return i;
            } else {
                Message next = messages.get(i);
                while (next != null && next.wasMergedIntoPrevious()) {
                    if (uuid.equals(next.getUuid())) {
                        return i;
                    }
                    next = next.next();
                }

            }
        }
        return -1;
    }

    private ScrollState getScrollPosition() {
        final ListView listView = this.binding == null ? null : this.binding.messagesView;
        if (listView == null || listView.getCount() == 0 || listView.getLastVisiblePosition() == listView.getCount() - 1) {
            return null;
        } else {
            final int pos = listView.getFirstVisiblePosition();
            final View view = listView.getChildAt(0);
            if (view == null) {
                return null;
            } else {
                return new ScrollState(pos, view.getTop());
            }
        }
    }

    private void setScrollPosition(ScrollState scrollPosition, String lastMessageUuid) {
        if (scrollPosition != null) {
            this.lastMessageUuid = lastMessageUuid;
            if (lastMessageUuid != null) {
                binding.unreadCountCustomView.setUnreadCount(conversation.getReceivedMessagesCountSinceUuid(lastMessageUuid));
            }
            //TODO maybe this needs a 'post'
            this.binding.messagesView.setSelectionFromTop(scrollPosition.position, scrollPosition.offset);
            toggleScrollDownButton();
        }
    }

    private void attachLocationToConversation(Conversation conversation, Uri uri) {
        if (conversation == null) {
            return;
        }
        activity.xmppConnectionService.attachLocationToConversation(conversation, uri, new UiCallback<Message>() {

            @Override
            public void success(Message message) {

            }

            @Override
            public void error(int errorCode, Message object) {
                //TODO show possible pgp error
            }

            @Override
            public void userInputRequired(PendingIntent pi, Message object) {

            }

            @Override
            public void progress(int progress) {

            }
        });
    }

    private void attachFileToConversation(Conversation conversation, Uri uri, String type) {
        if (conversation == null) {
            return;
        }
        final Toast prepareFileToast = ToastCompat.makeText(getActivity(), getText(R.string.preparing_file), ToastCompat.LENGTH_SHORT);
        prepareFileToast.show();
        activity.delegateUriPermissionsToService(uri);
        activity.xmppConnectionService.attachFileToConversation(conversation, uri, type, new UiInformableCallback<Message>() {
            @Override
            public void inform(final String text) {
                hidePrepareFileToast(prepareFileToast);
                runOnUiThread(() -> activity.replaceToast(text));
            }

            @Override
            public void success(Message message) {
                runOnUiThread(() -> {
                    activity.hideToast();
                    setupReply(null);
                });
                hidePrepareFileToast(prepareFileToast);
            }

            @Override
            public void error(final int errorCode, Message message) {
                hidePrepareFileToast(prepareFileToast);
                runOnUiThread(() -> activity.replaceToast(getString(errorCode)));

            }

            @Override
            public void userInputRequired(PendingIntent pi, Message message) {
                hidePrepareFileToast(prepareFileToast);
            }

            @Override
            public void progress(int progress) {
                hidePrepareFileToast(prepareFileToast);
                updateSnackBar(conversation);
            }
        });
    }

    public void attachEditorContentToConversation(Uri uri) {
        mediaPreviewAdapter.addMediaPreviews(Attachment.of(getActivity(), uri, Attachment.Type.FILE));
        toggleInputMethod();
    }

    private void attachImageToConversation(Conversation conversation, Uri uri, String type) {
        if (conversation == null) {
            return;
        }
        final Toast prepareFileToast = ToastCompat.makeText(getActivity(), getText(R.string.preparing_image), ToastCompat.LENGTH_LONG);
        prepareFileToast.show();
        activity.delegateUriPermissionsToService(uri);
        activity.xmppConnectionService.attachImageToConversation(conversation, uri, type,
                new UiCallback<Message>() {
                    @Override
                    public void userInputRequired(PendingIntent pi, Message object) {
                        hidePrepareFileToast(prepareFileToast);
                    }

                    @Override
                    public void progress(int progress) {

                    }

                    @Override
                    public void success(Message message) {
                        hidePrepareFileToast(prepareFileToast);
                        runOnUiThread(() -> setupReply(null));
                    }

                    @Override
                    public void error(final int error, final Message message) {
                        hidePrepareFileToast(prepareFileToast);
                        final ConversationsActivity activity = ConversationFragment.this.activity;
                        if (activity == null) {
                            return;
                        }
                        activity.runOnUiThread(() -> activity.replaceToast(getString(error)));
                    }
                });
    }

    private void hidePrepareFileToast(final Toast prepareFileToast) {
        if (prepareFileToast != null && activity != null) {
            activity.runOnUiThread(prepareFileToast::cancel);
        }
    }

    private void sendMessage() {
        if (mediaPreviewAdapter.hasAttachments()) {
            commitAttachments();
            return;
        }
        final Editable text = this.binding.textinput.getText();
        String body = text == null ? "" : text.toString();
        final Conversation conversation = this.conversation;
        if (body.length() == 0 || conversation == null) {
            return;
        }
        if (trustKeysIfNeeded(conversation, REQUEST_TRUST_KEYS_TEXT)) {
            return;
        }
        final Message message;
        if (conversation.getCorrectingMessage() == null) {
            boolean attention = false;
            if (Pattern.compile("\\A@here\\s.*").matcher(body).find()) {
                attention = true;
                body = body.replaceFirst("\\A@here\\s+", "");
            }
            if (conversation.getReplyTo() != null) {
                if (Emoticons.isEmoji(body)) {
                    message = conversation.getReplyTo().react(body);
                } else {
                    message = conversation.getReplyTo().reply();
                    message.appendBody(body);
                }
                message.setEncryption(conversation.getNextEncryption());
            } else {
                message = new Message(conversation, body, conversation.getNextEncryption());
            }
            message.setThread(conversation.getThread());
            if (attention) {
                message.addPayload(new Element("attention", "urn:xmpp:attention:0"));
            }
            Message.configurePrivateMessage(message);
        } else {
            message = conversation.getCorrectingMessage();
            message.setBody(body);
            message.setThread(conversation.getThread());
            message.putEdited(message.getUuid(), message.getServerMsgId(), message.getBody(), message.getTimeSent());
            message.setServerMsgId(null);
            message.setUuid(UUID.randomUUID().toString());
        }
        switch (conversation.getNextEncryption()) {
            case Message.ENCRYPTION_OTR:
                sendOtrMessage(message);
                break;
            case Message.ENCRYPTION_PGP:
                sendPgpMessage(message);
                break;
            default:
                sendMessage(message);
        }
        setupReply(null);
    }

    private boolean trustKeysIfNeeded(final Conversation conversation, final int requestCode) {
        return conversation.getNextEncryption() == Message.ENCRYPTION_AXOLOTL && trustKeysIfNeeded(requestCode);
    }

    protected boolean trustKeysIfNeeded(int requestCode) {
        AxolotlService axolotlService = conversation.getAccount().getAxolotlService();
        final List<Jid> targets = axolotlService.getCryptoTargets(conversation);
        boolean hasUnaccepted = !conversation.getAcceptedCryptoTargets().containsAll(targets);
        boolean hasUndecidedOwn = !axolotlService.getKeysWithTrust(FingerprintStatus.createActiveUndecided()).isEmpty();
        boolean hasUndecidedContacts = !axolotlService.getKeysWithTrust(FingerprintStatus.createActiveUndecided(), targets).isEmpty();
        boolean hasPendingKeys = !axolotlService.findDevicesWithoutSession(conversation).isEmpty();
        boolean hasNoTrustedKeys = axolotlService.anyTargetHasNoTrustedKeys(targets);
        boolean downloadInProgress = axolotlService.hasPendingKeyFetches(targets);
        if (hasUndecidedOwn || hasUndecidedContacts || hasPendingKeys || hasNoTrustedKeys || hasUnaccepted || downloadInProgress) {
            axolotlService.createSessionsIfNeeded(conversation);
            Intent intent = new Intent(getActivity(), TrustKeysActivity.class);
            String[] contacts = new String[targets.size()];
            for (int i = 0; i < contacts.length; ++i) {
                contacts[i] = targets.get(i).toString();
            }
            intent.putExtra("contacts", contacts);
            intent.putExtra(EXTRA_ACCOUNT, conversation.getAccount().getJid().asBareJid().toEscapedString());
            intent.putExtra("conversation", conversation.getUuid());
            startActivityForResult(intent, requestCode);
            return true;
        } else {
            return false;
        }
    }

    public void updateChatMsgHint() {
        final boolean multi = conversation.getMode() == Conversation.MODE_MULTI;
        if (conversation.getCorrectingMessage() != null) {
            this.binding.textInputHint.setVisibility(View.VISIBLE);
            this.binding.textInputHint.setText(R.string.send_corrected_message);
            this.binding.textinput.setHint(R.string.send_corrected_message);
        } else if (isPrivateMessage()) {
            this.binding.textinput.setHint(R.string.send_unencrypted_message);
            this.binding.textInputHint.setVisibility(View.VISIBLE);
            SpannableStringBuilder hint = new SpannableStringBuilder(getString(R.string.send_private_message_to, conversation.getNextCounterpart().getResource()));
            hint.setSpan(new StyleSpan(Typeface.BOLD), 0, hint.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            this.binding.textInputHint.setText(hint);
        } else if (multi && !conversation.getMucOptions().participating()) {
            this.binding.textInputHint.setVisibility(View.VISIBLE);
            this.binding.textInputHint.setText(R.string.ask_for_writeaccess);
            this.binding.textinput.setHint(R.string.you_are_not_participating);
        } else {
            this.binding.textInputHint.setVisibility(View.GONE);
            if (getActivity() != null) {
                this.binding.textinput.setHint(UIHelper.getMessageHint(getActivity(), conversation));
                getActivity().invalidateOptionsMenu();
            }
        }
    }

    private boolean isPrivateMessage() {
        return conversation != null && conversation.getMode() == Conversation.MODE_MULTI && conversation.getNextCounterpart() != null;
    }

    public void setupIme() {
        this.binding.textinput.refreshIme();
    }

    private void handleActivityResult(ActivityResult activityResult) {
        if (activityResult.resultCode == Activity.RESULT_OK) {
            handlePositiveActivityResult(activityResult.requestCode, activityResult.data);
        } else {
            handleNegativeActivityResult(activityResult.requestCode);
        }
    }

    private void handlePositiveActivityResult(int requestCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_TRUST_KEYS_TEXT:
                sendMessage();
                break;
            case REQUEST_TRUST_KEYS_ATTACHMENTS:
                commitAttachments();
                break;
            case REQUEST_START_AUDIO_CALL:
                triggerRtpSession(RtpSessionActivity.ACTION_MAKE_VOICE_CALL);
                break;
            case REQUEST_START_VIDEO_CALL:
                triggerRtpSession(RtpSessionActivity.ACTION_MAKE_VIDEO_CALL);
                break;
            case ATTACHMENT_CHOICE_CHOOSE_IMAGE:
                final List<Attachment> imageUris = Attachment.extractAttachments(getActivity(), data, Attachment.Type.IMAGE);
                mediaPreviewAdapter.addMediaPreviews(imageUris);
                toggleInputMethod();
                break;
            case ATTACHMENT_CHOICE_TAKE_PHOTO:
                final Uri takePhotoUri = pendingTakePhotoUri.pop();
                if (takePhotoUri != null) {
                    mediaPreviewAdapter.addMediaPreviews(Attachment.of(getActivity(), takePhotoUri, Attachment.Type.IMAGE));
                    activity.xmppConnectionService.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, takePhotoUri));
                    toggleInputMethod();
                } else {
                    Log.d(Config.LOGTAG, "lost take photo uri. unable to to attach");
                }
                break;
            case ATTACHMENT_CHOICE_CHOOSE_FILE:
            case ATTACHMENT_CHOICE_RECORD_VIDEO:
            case ATTACHMENT_CHOICE_RECORD_VOICE:
            case ATTACHMENT_CHOICE_CHOOSE_VIDEO:
                final Attachment.Type type = requestCode == ATTACHMENT_CHOICE_RECORD_VOICE ? Attachment.Type.RECORDING : Attachment.Type.FILE;
                final List<Attachment> fileUris = Attachment.extractAttachments(getActivity(), data, type);
                mediaPreviewAdapter.addMediaPreviews(fileUris);
                toggleInputMethod();
                break;
            case ATTACHMENT_CHOICE_LOCATION:
                final double latitude = data.getDoubleExtra("latitude", 0);
                final double longitude = data.getDoubleExtra("longitude", 0);
                final int accuracy = data.getIntExtra("accuracy", 0);
                final Uri geo;
                if (accuracy > 0) {
                    geo = Uri.parse(String.format("geo:%s,%s;u=%s", latitude, longitude, accuracy));
                } else {
                    geo = Uri.parse(String.format("geo:%s,%s", latitude, longitude));
                }
                mediaPreviewAdapter.addMediaPreviews(Attachment.of(getActivity(), geo, Attachment.Type.LOCATION));
                toggleInputMethod();
                break;
            case REQUEST_INVITE_TO_CONVERSATION:
                XmppActivity.ConferenceInvite invite = XmppActivity.ConferenceInvite.parse(data);
                if (invite != null && activity != null) {
                    if (invite.execute(activity)) {
                        activity.mToast = ToastCompat.makeText(activity, R.string.creating_conference, ToastCompat.LENGTH_LONG);
                        activity.mToast.show();
                    }
                }
                break;
        }
    }

    private void commitAttachments() {
        final List<Attachment> attachments = mediaPreviewAdapter.getAttachments();
        if (anyNeedsExternalStoragePermission(attachments) && !hasPermissions(REQUEST_COMMIT_ATTACHMENTS, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            return;
        }
        if (trustKeysIfNeeded(conversation, REQUEST_TRUST_KEYS_ATTACHMENTS)) {
            return;
        }
        final PresenceSelector.OnPresenceSelected callback = () -> {
            for (Iterator<Attachment> i = attachments.iterator(); i.hasNext(); i.remove()) {
                final Attachment attachment = i.next();
                if (attachment.getType() == Attachment.Type.LOCATION) {
                    attachLocationToConversation(conversation, attachment.getUri());
                } else if (attachment.getType() == Attachment.Type.IMAGE) {
                    Log.d(Config.LOGTAG, "ConversationsActivity.commitAttachments() - attaching image to conversations. CHOOSE_IMAGE");
                    attachImageToConversation(conversation, attachment.getUri(), attachment.getMime());
                } else {
                    Log.d(Config.LOGTAG, "ConversationsActivity.commitAttachments() - attaching file to conversations. CHOOSE_FILE/RECORD_VOICE/RECORD_VIDEO");
                    attachFileToConversation(conversation, attachment.getUri(), attachment.getMime());
                }
            }
            mediaPreviewAdapter.notifyDataSetChanged();
            toggleInputMethod();
        };
        if (conversation == null
                || conversation.getMode() == Conversation.MODE_MULTI
                || Attachment.canBeSendInband(attachments)
                || (conversation.getAccount().httpUploadAvailable() && FileBackend.allFilesUnderSize(getActivity(), attachments, getMaxHttpUploadSize(conversation)))) {
            callback.onPresenceSelected();
        } else {
            activity.selectPresence(conversation, callback);
        }
    }


    private static boolean anyNeedsExternalStoragePermission(final Collection<Attachment> attachments) {
        for (final Attachment attachment : attachments) {
            if (attachment.getType() != Attachment.Type.LOCATION) {
                return true;
            }
        }
        return false;
    }

    public void toggleInputMethod() {
        boolean hasAttachments = mediaPreviewAdapter.hasAttachments();
        binding.textinput.setVisibility(hasAttachments ? View.GONE : View.VISIBLE);
        binding.mediaPreview.setVisibility(hasAttachments ? View.VISIBLE : View.GONE);
        if (mOptionsMenu != null) {
            ConversationMenuConfigurator.configureAttachmentMenu(conversation, mOptionsMenu, activity.getAttachmentChoicePreference(), hasAttachments);
        }
        updateSendButton();
    }

    private boolean canSendMeCommand() {
        if (conversation != null) {
            final String body = binding.textinput.getText().toString();
            return body.length() == 0;
        }
        return false;
    }

    private void handleNegativeActivityResult(int requestCode) {
        switch (requestCode) {
            case ATTACHMENT_CHOICE_TAKE_PHOTO:
                if (pendingTakePhotoUri.clear()) {
                    Log.d(Config.LOGTAG, "cleared pending photo uri after negative activity result");
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ActivityResult activityResult = ActivityResult.of(requestCode, resultCode, data);
        if (activity != null && activity.xmppConnectionService != null) {
            handleActivityResult(activityResult);
        } else {
            this.postponedActivityResult.push(activityResult);
        }
    }

    public void unblockConversation(final Blockable conversation) {
        activity.xmppConnectionService.sendUnblockRequest(conversation);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(Config.LOGTAG, "ConversationFragment.onAttach()");
        if (activity instanceof ConversationsActivity) {
            this.activity = (ConversationsActivity) activity;
        } else {
            throw new IllegalStateException("Trying to attach fragment to activity that is not the ConversationsActivity");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null; //TODO maybe not a good idea since some callbacks really need it
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        mOptionsMenu = menu;
        boolean hasAttachments = mediaPreviewAdapter != null && mediaPreviewAdapter.hasAttachments();
        menuInflater.inflate(R.menu.fragment_conversation, menu);
        final MenuItem menuInviteContact = menu.findItem(R.id.action_invite);
        final MenuItem menuNeedHelp = menu.findItem(R.id.action_create_issue);
        final MenuItem menuSearchUpdates = menu.findItem(R.id.action_check_updates);
        final MenuItem menuArchiveChat = menu.findItem(R.id.action_archive_chat);
        final MenuItem menuGroupDetails = menu.findItem(R.id.action_group_details);
        final MenuItem menuParticipants = menu.findItem(R.id.action_participants);
        final MenuItem menuContactDetails = menu.findItem(R.id.action_contact_details);
        final MenuItem menuCall = menu.findItem(R.id.action_call);
        final MenuItem menuOngoingCall = menu.findItem(R.id.action_ongoing_call);
        final MenuItem menuVideoCall = menu.findItem(R.id.action_video_call);
        final MenuItem menuMediaBrowser = menu.findItem(R.id.action_mediabrowser);
        final MenuItem menuTogglePinned = menu.findItem(R.id.action_toggle_pinned);

        if (conversation != null) {
            if (conversation.getMode() == Conversation.MODE_MULTI || (activity.xmppConnectionService != null && !activity.xmppConnectionService.hasInternetConnection())) {
                menuInviteContact.setVisible(conversation.getMucOptions().canInvite());
                menuArchiveChat.setTitle(R.string.action_end_conversation_muc);
                menuCall.setVisible(false);
                menuOngoingCall.setVisible(false);
                menuParticipants.setVisible(true);
            } else {
                final XmppConnectionService service = activity == null ? null : activity.xmppConnectionService;
                final Optional<OngoingRtpSession> ongoingRtpSession = service == null ? Optional.absent() : service.getJingleConnectionManager().getOngoingRtpConnection(conversation.getContact());
                if (ongoingRtpSession.isPresent()) {
                    menuOngoingCall.setVisible(true);
                    menuCall.setVisible(false);
                } else {
                    menuOngoingCall.setVisible(false);
                    final RtpCapability.Capability rtpCapability = RtpCapability.check(conversation.getContact());
                    final boolean cameraAvailable = activity != null && activity.isCameraFeatureAvailable();
                    menuCall.setVisible(rtpCapability != RtpCapability.Capability.NONE);
                    menuVideoCall.setVisible(rtpCapability == RtpCapability.Capability.VIDEO && cameraAvailable);
                }
                menuParticipants.setVisible(false);
                menuInviteContact.setVisible(false);
                menuArchiveChat.setTitle(R.string.action_end_conversation);
            }
            try {
                Fragment secondaryFragment = activity.getFragmentManager().findFragmentById(R.id.secondary_fragment);
                if (secondaryFragment instanceof ConversationFragment) {
                    if (conversation.getMode() == Conversation.MODE_MULTI) {
                        menuGroupDetails.setTitle(conversation.getMucOptions().isPrivateAndNonAnonymous() ? R.string.action_group_details : R.string.channel_details);
                        menuGroupDetails.setVisible(true);
                        menuContactDetails.setVisible(false);
                    } else {
                        menuGroupDetails.setVisible(false);
                        menuContactDetails.setVisible(!this.conversation.withSelf());
                    }
                    menuSearchUpdates.setVisible(true);
                } else {
                    menuGroupDetails.setVisible(false);
                    menuContactDetails.setVisible(false);
                    menuSearchUpdates.setVisible(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                menuGroupDetails.setVisible(false);
                menuContactDetails.setVisible(false);
                menuSearchUpdates.setVisible(false);
            }
            menuMediaBrowser.setVisible(true);
            menuNeedHelp.setVisible(false);
            ConversationMenuConfigurator.configureAttachmentMenu(conversation, menu, activity.getAttachmentChoicePreference(), hasAttachments);
            ConversationMenuConfigurator.configureEncryptionMenu(conversation, menu, activity);
            if (conversation.getBooleanAttribute(Conversation.ATTRIBUTE_PINNED_ON_TOP, false)) {
                menuTogglePinned.setTitle(R.string.remove_from_favorites);
            } else {
                menuTogglePinned.setTitle(R.string.add_to_favorites);
            }
        } else {
            menuNeedHelp.setVisible(true);
            menuSearchUpdates.setVisible(true);
            menuInviteContact.setVisible(false);
            menuGroupDetails.setVisible(false);
            menuContactDetails.setVisible(false);
            menuMediaBrowser.setVisible(false);
        }
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_conversation, container, false);
        binding.getRoot().setOnClickListener(null); //TODO why the fuck did we do this?

        binding.textinput.addTextChangedListener(new StylingHelper.MessageEditorStyler(binding.textinput));
        binding.textinput.setOnEditorActionListener(mEditorActionListener);
        binding.textinput.setRichContentListener(new String[]{"image/*"}, mEditorContentListener);

        binding.textSendButton.setOnClickListener(this.mSendButtonListener);
        binding.contextPreviewCancel.setOnClickListener((v) -> {
            setupReply(null);
        });
        binding.textSendButton.setOnLongClickListener(this.mSendButtonLongListener);
        binding.scrollToBottomButton.setOnClickListener(this.mScrollButtonListener);
        binding.recordVoiceButton.setOnClickListener(this.mRecordVoiceButtonListener);

        binding.messagesView.setOnScrollListener(mOnScrollListener);
        binding.messagesView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        mediaPreviewAdapter = new MediaPreviewAdapter(this);
        binding.mediaPreview.setAdapter(mediaPreviewAdapter);
        messageListAdapter = new MessageAdapter((XmppActivity) getActivity(), this.messageList);
        messageListAdapter.setOnContactPictureClicked(this);
        messageListAdapter.setOnContactPictureLongClicked(this);
        binding.messagesView.setAdapter(messageListAdapter);
        registerForContextMenu(binding.messagesView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.binding.textinput.setCustomInsertionActionModeCallback(new EditMessageActionModeCallback(this.binding.textinput));
        }
        messageListAdapter.setOnMessageBoxSwiped(message -> {
            String user = null;
            quoteMessage(message, user);
        });
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(Config.LOGTAG, "ConversationFragment.onDestroyView()");
        messageListAdapter.setOnContactPictureClicked(null);
        messageListAdapter.setOnContactPictureLongClicked(null);
    }

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        int animator = enter ? R.animator.fade_right_in : R.animator.fade_right_out;
        return AnimatorInflater.loadAnimator(this.activity, animator);

    }

    private void quoteText(String text, String user) {
        if (binding.textinput.isEnabled()) {
            String username = "";
            if (user != null && user.length() > 0) {
                if (user.equals(getString(R.string.me))) {
                    username = getString(R.string.me_quote) + System.getProperty("line.separator");
                } else {
                    username = getString(R.string.x_user_quote, user) + System.getProperty("line.separator");
                }
            }
            binding.textinput.insertAsQuote(username + text);
            binding.textinput.requestFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.showSoftInput(binding.textinput, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    private void showRecordVoiceButton() {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(activity);
        final boolean ShowRecordVoiceButton = p.getBoolean("show_record_voice_btn", activity.getResources().getBoolean(R.bool.show_record_voice_btn));
        Log.d(Config.LOGTAG, "Recorder " + ShowRecordVoiceButton);
        if (ShowRecordVoiceButton) {
            binding.recordVoiceButton.setVisibility(View.VISIBLE);
        } else {
            binding.recordVoiceButton.setVisibility(View.GONE);
        }
        binding.recordVoiceButton.setImageResource(activity.getThemeResource(R.attr.ic_send_voice_offline, R.drawable.ic_send_voice_offline));
    }

    private void quoteMedia(Message message, @Nullable String user) {
        Message.FileParams params = message.getFileParams();
        String filesize = params.size != null ? UIHelper.filesizeToString(params.size) : null;
        final StringBuilder stringBuilder = new StringBuilder();
        if (activity.showDateInQuotes()) {
            stringBuilder.append(df.format(message.getTimeSent())).append(System.getProperty("line.separator"));
        }
        stringBuilder.append(MimeUtils.getMimeTypeEmoji(getActivity(), message.getMimeType())).append(" ");
        stringBuilder.append(" \u00B7 ");
        stringBuilder.append(filesize);
        quoteText(stringBuilder.toString(), user);
    }

    private void quoteGeoUri(Message message, @Nullable String user) {
        final StringBuilder stringBuilder = new StringBuilder();
        if (activity.showDateInQuotes()) {
            stringBuilder.append(df.format(message.getTimeSent())).append(System.getProperty("line.separator"));
        }
        stringBuilder.append("\uD83D\uDDFA"); // map
        quoteText(stringBuilder.toString(), user);
    }

    private void quoteMessage(Message message, @Nullable String user) {
        if (message.isGeoUri()) {
            quoteGeoUri(message, user);
        }
        setupReply(message);
    }

    private void setupReply(Message message) {
        conversation.setReplyTo(message);
        if (message == null) {
            binding.contextPreview.setVisibility(View.GONE);
            return;
        }

        SpannableStringBuilder body = message.getSpannableBody(null, null);
        if (message.isFileOrImage() || message.isOOb()) body.append(" 🖼️");
        messageListAdapter.handleTextQuotes(body, activity.isDarkTheme());
        binding.contextPreviewText.setText(body);
        binding.contextPreview.setVisibility(View.VISIBLE);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        //This should cancel any remaining click events that would otherwise trigger links
        v.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0f, 0f, 0));
        synchronized (this.messageList) {
            super.onCreateContextMenu(menu, v, menuInfo);
            AdapterView.AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) menuInfo;
            this.selectedMessage = this.messageList.get(acmi.position);
            populateContextMenu(menu);
        }
    }

    private void populateContextMenu(ContextMenu menu) {
        final Message m = this.selectedMessage;
        final Transferable t = m.getTransferable();
        Message relevantForCorrection = m;
        while (relevantForCorrection.mergeable(relevantForCorrection.next())) {
            relevantForCorrection = relevantForCorrection.next();
        }
        if (m.getType() != Message.TYPE_STATUS && m.getType() != Message.TYPE_RTP_SESSION) {

            if (m.getEncryption() == Message.ENCRYPTION_AXOLOTL_NOT_FOR_THIS_DEVICE || m.getEncryption() == Message.ENCRYPTION_AXOLOTL_FAILED) {
                return;
            }
            if (m.getStatus() == Message.STATUS_RECEIVED && t != null && (t.getStatus() == Transferable.STATUS_CANCELLED || t.getStatus() == Transferable.STATUS_FAILED)) {
                return;
            }
            final boolean fileDeleted = m.isFileDeleted();
            final boolean encrypted = m.getEncryption() == Message.ENCRYPTION_DECRYPTION_FAILED
                    || m.getEncryption() == Message.ENCRYPTION_PGP;
            final boolean receiving = m.getStatus() == Message.STATUS_RECEIVED && (t instanceof JingleFileTransferConnection || t instanceof HttpDownloadConnection);
            activity.getMenuInflater().inflate(R.menu.message_context, menu);
            MenuItem openWith = menu.findItem(R.id.open_with);
            MenuItem copyMessage = menu.findItem(R.id.copy_message);
            MenuItem quoteMessage = menu.findItem(R.id.quote_message);
            MenuItem retryDecryption = menu.findItem(R.id.retry_decryption);
            MenuItem correctMessage = menu.findItem(R.id.correct_message);
            MenuItem retractMessage = menu.findItem(R.id.retract_message);
            MenuItem moderateMessage = menu.findItem(R.id.moderate_message);
            MenuItem deleteMessage = menu.findItem(R.id.delete_message);
            MenuItem shareWith = menu.findItem(R.id.share_with);
            MenuItem sendAgain = menu.findItem(R.id.send_again);
            MenuItem copyUrl = menu.findItem(R.id.copy_url);
            MenuItem cancelTransmission = menu.findItem(R.id.cancel_transmission);
            MenuItem downloadFile = menu.findItem(R.id.download_file);
            MenuItem deleteFile = menu.findItem(R.id.delete_file);
            MenuItem showLog = menu.findItem(R.id.show_edit_log);
            MenuItem showErrorMessage = menu.findItem(R.id.show_error_message);
            MenuItem saveFile = menu.findItem(R.id.save_file);
            final boolean unInitiatedButKnownSize = MessageUtils.unInitiatedButKnownSize(m);
            final boolean showError = m.getStatus() == Message.STATUS_SEND_FAILED && m.getErrorMessage() != null && !Message.ERROR_MESSAGE_CANCELLED.equals(m.getErrorMessage());
            final boolean messageDeleted = m.isMessageDeleted();
            deleteMessage.setVisible(true);
            if (!encrypted && !m.getBody().equals("")) {
                copyMessage.setVisible(true);
            }
            quoteMessage.setVisible(!encrypted && !showError);
            if (m.getEncryption() == Message.ENCRYPTION_DECRYPTION_FAILED && !fileDeleted) {
                retryDecryption.setVisible(true);
            }
            if (!encrypted && !unInitiatedButKnownSize && t == null) {
                quoteMessage.setVisible(!showError && QuoteHelper.isMessageQuoteable(m));
            }
            if (!showError
                    && relevantForCorrection.getType() == Message.TYPE_TEXT
                    && !m.isGeoUri()
                    && relevantForCorrection.isLastCorrectableMessage()
                    && m.getConversation() instanceof Conversation) {
                correctMessage.setVisible(true);
                if (!relevantForCorrection.getBody().equals("") && !relevantForCorrection.getBody().equals(" ")) retractMessage.setVisible(true);
            }
            if (relevantForCorrection.getReactions() != null) {
                correctMessage.setVisible(false);
                retractMessage.setVisible(true);
            }
            if (conversation.getMode() == Conversation.MODE_MULTI && m.getServerMsgId() != null && m.getModerated() == null && conversation.getMucOptions().getSelf().getRole().ranks(MucOptions.Role.MODERATOR) && conversation.getMucOptions().hasFeature("urn:xmpp:message-moderate:0")) {
                moderateMessage.setVisible(true);
            }
            if ((m.isFileOrImage() && !fileDeleted && !receiving) || (m.getType() == Message.TYPE_TEXT && !m.treatAsDownloadable()) && !unInitiatedButKnownSize && t == null && !messageDeleted) {
                shareWith.setVisible(true);

            }
            if (m.getStatus() == Message.STATUS_SEND_FAILED) {
                sendAgain.setVisible(true);
            }
            if (m.hasFileOnRemoteHost()
                    || m.isGeoUri()
                    || m.isXmppUri()
                    || m.treatAsDownloadable()
                    || unInitiatedButKnownSize
                    || t instanceof HttpDownloadConnection) {
                copyUrl.setVisible(true);
            }
            if (m.isFileOrImage() && fileDeleted && m.hasFileOnRemoteHost()) {
                downloadFile.setVisible(true);
                downloadFile.setTitle(activity.getString(R.string.download_x_file, UIHelper.getFileDescriptionString(activity, m)));
            }
            final boolean waitingOfferedSending = m.getStatus() == Message.STATUS_WAITING
                    || m.getStatus() == Message.STATUS_UNSEND
                    || m.getStatus() == Message.STATUS_OFFERED;
            final boolean cancelable = (t != null && !fileDeleted) || waitingOfferedSending && m.needsUploading();
            if (cancelable) {
                cancelTransmission.setVisible(true);
            }
            if (m.isFileOrImage() && !fileDeleted && !cancelable) {
                final String path = m.getRelativeFilePath();
                Log.d(Config.LOGTAG, "Path = " + path);
                if (path == null || !path.startsWith("/") || path.contains(getConversationsDirectory(this.activity, "null").getAbsolutePath())) {
                    deleteFile.setVisible(true);
                    deleteFile.setTitle(activity.getString(R.string.delete_x_file, UIHelper.getFileDescriptionString(activity, m)));
                }
                saveFile.setVisible(true);
                saveFile.setTitle(activity.getString(R.string.save_x_file, UIHelper.getFileDescriptionString(activity, m)));
            }
            showErrorMessage.setVisible(showError);
            final String mime = m.isFileOrImage() ? m.getMimeType() : null;
            if ((m.isGeoUri() && GeoHelper.openInOsmAnd(getActivity(), m)) || (mime != null && mime.startsWith("audio/"))) {
                openWith.setVisible(true);
            }
            if (m.edited() && m.getRetractId() == null) {
                showLog.setVisible(true);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String user;
        try {
            final Contact contact = selectedMessage.getContact();
            if (conversation.getMode() == Conversation.MODE_MULTI) {
                user = UIHelper.getDisplayedMucCounterpart(selectedMessage.getCounterpart());
            } else {
                user = contact != null ? contact.getDisplayName() : null;
            }
            if (selectedMessage.getStatus() == Message.STATUS_SEND
                    || selectedMessage.getStatus() == Message.STATUS_SEND_FAILED
                    || selectedMessage.getStatus() == Message.STATUS_SEND_RECEIVED
                    || selectedMessage.getStatus() == Message.STATUS_SEND_DISPLAYED) {
                user = getString(R.string.me);
            }
        } catch (Exception e) {
            e.printStackTrace();
            user = null;
        }
        switch (item.getItemId()) {
            case R.id.share_with:
                ShareUtil.share(activity, selectedMessage, user);
                return true;
            case R.id.correct_message:
                correctMessage(selectedMessage);
                return true;
            case R.id.retract_message:
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.retract_message)
                        .setMessage("Do you really want to retract this message?")
                        .setPositiveButton(R.string.yes, (dialog, whichButton) -> {
                            Message message = selectedMessage;
                            while (message.mergeable(message.next())) {
                                message = message.next();
                            }
                            Element reactions = message.getReactions();
                            if (reactions != null) {
                                final Message previousReaction = conversation.findMessageReactingTo(reactions.getAttribute("id"), null);
                                if (previousReaction != null) reactions = previousReaction.getReactions();
                                for (Element el : reactions.getChildren()) {
                                    if (message.getQuoteableBody().endsWith(el.getContent())) {
                                        reactions.removeChild(el);
                                    }
                                }
                                message.setReactions(reactions);
                                if (previousReaction != null) {
                                    previousReaction.setReactions(reactions);
                                    activity.xmppConnectionService.updateMessage(previousReaction);
                                }
                            }
                            message.setBody(" ");
                            message.putEdited(message.getUuid(), message.getServerMsgId(), message.getBody(), message.getTimeSent());
                            message.setServerMsgId(null);
                            message.setUuid(UUID.randomUUID().toString());
                            sendMessage(message);
                        })
                        .setNegativeButton(R.string.no, null).show();
                return true;
            case R.id.moderate_message:
                activity.quickEdit("Spam", (reason) -> {
                    activity.xmppConnectionService.moderateMessage(conversation.getAccount(), selectedMessage, reason);
                    return null;
                }, R.string.moderate_reason, false, false, true);
                return true;
            case R.id.copy_message:
                ShareUtil.copyToClipboard(activity, selectedMessage);
                return true;
            case R.id.quote_message:
                if (conversation.getMode() == Conversation.MODE_MULTI) {
                    quoteMessage(selectedMessage, user);
                } else {
                    quoteMessage(selectedMessage, null);
                }
                return true;
            case R.id.send_again:
                resendMessage(selectedMessage);
                return true;
            case R.id.copy_url:
                ShareUtil.copyUrlToClipboard(activity, selectedMessage);
                return true;
            case R.id.download_file:
                startDownloadable(selectedMessage);
                return true;
            case R.id.cancel_transmission:
                cancelTransmission(selectedMessage);
                return true;
            case R.id.retry_decryption:
                retryDecryption(selectedMessage);
                return true;
            case R.id.delete_message:
                deleteMessage(selectedMessage);
                return true;
            case R.id.delete_file:
                deleteFile(selectedMessage);
                return true;
            case R.id.show_error_message:
                showErrorMessage(selectedMessage);
                return true;
            case R.id.open_with:
                openWith(selectedMessage);
                return true;
            case R.id.save_file:
                activity.xmppConnectionService.getFileBackend().saveFile(selectedMessage, activity);
                return true;
            case R.id.show_edit_log:
                openLog(selectedMessage);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void openLog(Message logMsg) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.show_edit_log);
        ArrayList dataModels = new ArrayList<>();
        for (Edit itm : logMsg.getEditedList()) {
            dataModels.add(new MessageLogModel(itm.getBody(), itm.getTimeSent()));
        }
        dataModels.add(new MessageLogModel(logMsg.getBody(), logMsg.getTimeSent()));

        MessageLogAdapter adapter = new MessageLogAdapter(dataModels, getActivity());

        LinearLayout layout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(layoutParams);

        ListView listView = new ListView(getActivity());
        listView.setLayoutParams(layoutParams);
        layout.addView(listView);

        builder.setView(layout);

        listView.setAdapter(adapter);

        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            dialog.dismiss();
        });
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        Activity mXmppActivity = getActivity();
        if (MenuDoubleTabUtil.shouldIgnoreTap()) {
            return false;
        } else if (conversation == null) {
            return super.onOptionsItemSelected(item);
        }
        switch (item.getItemId()) {
            case R.id.encryption_choice_axolotl:
            case R.id.encryption_choice_otr:
            case R.id.encryption_choice_pgp:
            case R.id.encryption_choice_none:
                handleEncryptionSelection(item);
                break;
            case R.id.attach_choose_picture:
            case R.id.attach_choose_video:
            case R.id.attach_take_picture:
            case R.id.attach_record_video:
            case R.id.attach_choose_file:
            case R.id.attach_record_voice:
            case R.id.attach_location:
                handleAttachmentSelection(item);
                break;
            case R.id.action_search:
                startSearch();
                break;
            case R.id.action_archive_chat:
                if (conversation.getMode() == Conversation.MODE_SINGLE) {
                    activity.xmppConnectionService.archiveConversation(conversation);
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(activity.getString(R.string.action_end_conversation_muc));
                    builder.setMessage(activity.getString(R.string.leave_conference_warning));
                    builder.setNegativeButton(activity.getString(R.string.cancel), null);
                    builder.setPositiveButton(activity.getString(R.string.action_end_conversation_muc),
                            (dialog, which) -> {
                                activity.xmppConnectionService.archiveConversation(conversation);
                            });
                    builder.create().show();
                }
                break;
            case R.id.action_invite:
                startActivityForResult(ChooseContactActivity.create(activity, conversation), REQUEST_INVITE_TO_CONVERSATION);
                break;
            case R.id.action_clear_history:
                clearHistoryDialog(conversation);
                break;
            case R.id.action_group_details:
                activity.switchToMUCDetails(conversation);
                break;
            case R.id.action_participants:
                Intent intent1 = new Intent(activity, MucUsersActivity.class);
                intent1.putExtra("uuid", conversation.getUuid());
                startActivity(intent1);
                activity.overridePendingTransition(R.animator.fade_in, R.animator.fade_out);
                break;
            case R.id.action_contact_details:
                activity.switchToContactDetails(conversation.getContact());
                break;
            case R.id.action_mediabrowser:
                MediaBrowserActivity.launch(activity, conversation);
                break;
            case R.id.action_block:
            case R.id.action_unblock:
                if (mXmppActivity instanceof XmppActivity) {
                    BlockContactDialog.show((XmppActivity) mXmppActivity, conversation);
                }
                break;
            case R.id.action_audio_call:
                if (mXmppActivity instanceof XmppActivity) {
                    CallManager.checkPermissionAndTriggerAudioCall((XmppActivity) mXmppActivity, conversation);
                }
                break;
            case R.id.action_video_call:
                if (mXmppActivity instanceof XmppActivity) {
                    CallManager.checkPermissionAndTriggerVideoCall((XmppActivity) mXmppActivity, conversation);
                }
                break;
            case R.id.action_ongoing_call:
                if (mXmppActivity instanceof XmppActivity) {
                    CallManager.returnToOngoingCall((XmppActivity) mXmppActivity, conversation);
                }
                break;
            case R.id.action_toggle_pinned:
                togglePinned();
                break;
            case R.id.action_mute:
                muteConversationDialog(conversation);
                break;
            case R.id.action_unmute:
                unmuteConversation(conversation);
                break;
            case R.id.action_refresh_feature_discovery:
                refreshFeatureDiscovery();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startSearch() {
        final Intent intent = new Intent(getActivity(), SearchActivity.class);
        intent.putExtra(SearchActivity.EXTRA_CONVERSATION_UUID, conversation.getUuid());
        startActivity(intent);
    }
    private void returnToOngoingCall() {
        final Optional<OngoingRtpSession> ongoingRtpSession = activity.xmppConnectionService.getJingleConnectionManager().getOngoingRtpConnection(conversation.getContact());
        if (ongoingRtpSession.isPresent()) {
            final OngoingRtpSession id = ongoingRtpSession.get();
            final Intent intent = new Intent(activity, RtpSessionActivity.class);
            intent.putExtra(RtpSessionActivity.EXTRA_ACCOUNT, id.getAccount().getJid().asBareJid().toEscapedString());
            intent.putExtra(RtpSessionActivity.EXTRA_WITH, id.getWith().toEscapedString());
            if (id instanceof AbstractJingleConnection.Id) {
                intent.setAction(Intent.ACTION_VIEW);
                intent.putExtra(RtpSessionActivity.EXTRA_SESSION_ID, id.getSessionId());
            } else if (id instanceof JingleConnectionManager.RtpSessionProposal) {
                if (((JingleConnectionManager.RtpSessionProposal) id).media.contains(Media.VIDEO)) {
                    intent.setAction(RtpSessionActivity.ACTION_MAKE_VIDEO_CALL);
                } else {
                    intent.setAction(RtpSessionActivity.ACTION_MAKE_VOICE_CALL);
                }
            }
            activity.startActivity(intent);
        }

    }

    private void refreshFeatureDiscovery() {
        for (Map.Entry<String, Presence> entry : conversation.getContact().getPresences().getPresencesMap().entrySet()) {
            Jid jid = conversation.getContact().getJid();
            if (!entry.getKey().equals("")) jid = jid.withResource(entry.getKey());
            activity.xmppConnectionService.fetchCaps(conversation.getAccount(), jid, entry.getValue(), () -> {
                if (activity == null) return;
                activity.runOnUiThread(() -> {
                    refresh();
                    refreshCommands();
                });
            });
        }
    }

    private void togglePinned() {
        final boolean pinned = conversation.getBooleanAttribute(Conversation.ATTRIBUTE_PINNED_ON_TOP, false);
        conversation.setAttribute(Conversation.ATTRIBUTE_PINNED_ON_TOP, !pinned);
        activity.xmppConnectionService.updateConversation(conversation);
        activity.invalidateOptionsMenu();
    }

    private void checkPermissionAndTriggerAudioCall() {
        if (activity.mUseTor || conversation.getAccount().isOnion()) {
            Toast.makeText(activity, R.string.disable_tor_to_make_call, Toast.LENGTH_SHORT).show();
            return;
        }
        if (activity.mUseI2P || conversation.getAccount().isI2P()) {
            Toast.makeText(activity, R.string.no_i2p_calls, Toast.LENGTH_SHORT).show();
            return;
        }
        final List<String> permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions =
                    Arrays.asList(
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            permissions = Collections.singletonList(Manifest.permission.RECORD_AUDIO);
        }
        if (hasPermissions(REQUEST_START_AUDIO_CALL, permissions)) {
            triggerRtpSession(RtpSessionActivity.ACTION_MAKE_VOICE_CALL);
        }
    }

    private void checkPermissionAndTriggerVideoCall() {
        if (activity.mUseTor || conversation.getAccount().isOnion()) {
            Toast.makeText(activity, R.string.disable_tor_to_make_call, Toast.LENGTH_SHORT).show();
            return;
        }
        if (activity.mUseI2P || conversation.getAccount().isI2P()) {
            Toast.makeText(activity, R.string.no_i2p_calls, Toast.LENGTH_SHORT).show();
            return;
        }
        if (hasPermissions(REQUEST_START_VIDEO_CALL, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)) {
            triggerRtpSession(RtpSessionActivity.ACTION_MAKE_VIDEO_CALL);
        }
    }

    private void triggerRtpSession(final String action) {
        if (activity.xmppConnectionService.getJingleConnectionManager().isBusy()) {
            Toast.makeText(getActivity(), R.string.only_one_call_at_a_time, Toast.LENGTH_LONG).show();
            return;
        }
        final Contact contact = conversation.getContact();
        if (contact.getPresences().anySupport(Namespace.JINGLE_MESSAGE)) {
            triggerRtpSession(contact.getAccount(), contact.getJid().asBareJid(), action);
        } else {
            final RtpCapability.Capability capability;
            if (action.equals(RtpSessionActivity.ACTION_MAKE_VIDEO_CALL)) {
                capability = RtpCapability.Capability.VIDEO;
            } else {
                capability = RtpCapability.Capability.AUDIO;
            }
            PresenceSelector.selectFullJidForDirectRtpConnection(activity, contact, capability, fullJid -> {
                triggerRtpSession(contact.getAccount(), fullJid, action);
            });
        }
    }

    private void triggerRtpSession(final Account account, final Jid with, final String action) {
        final Intent intent = new Intent(activity, RtpSessionActivity.class);
        intent.setAction(action);
        intent.putExtra(RtpSessionActivity.EXTRA_ACCOUNT, account.getJid().toEscapedString());
        intent.putExtra(RtpSessionActivity.EXTRA_WITH, with.toEscapedString());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void handleAttachmentSelection(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.attach_choose_picture:
                attachFile(ATTACHMENT_CHOICE_CHOOSE_IMAGE);
                break;
            case R.id.attach_choose_video:
                attachFile(ATTACHMENT_CHOICE_CHOOSE_VIDEO);
                break;
            case R.id.attach_take_picture:
                attachFile(ATTACHMENT_CHOICE_TAKE_PHOTO);
                break;
            case R.id.attach_record_video:
                attachFile(ATTACHMENT_CHOICE_RECORD_VIDEO);
                break;
            case R.id.attach_choose_file:
                attachFile(ATTACHMENT_CHOICE_CHOOSE_FILE);
                break;
            case R.id.attach_record_voice:
                attachFile(ATTACHMENT_CHOICE_RECORD_VOICE);
                break;
            case R.id.attach_location:
                attachFile(ATTACHMENT_CHOICE_LOCATION);
                break;
        }
    }

    private void handleEncryptionSelection(MenuItem item) {
        if (conversation == null) {
            return;
        }
        final boolean updated;
        switch (item.getItemId()) {
            case R.id.encryption_choice_none:
                updated = conversation.setNextEncryption(Message.ENCRYPTION_NONE);
                item.setChecked(true);
                break;
            case R.id.encryption_choice_otr:
                updated = conversation.setNextEncryption(Message.ENCRYPTION_OTR);
                item.setChecked(true);
                break;
            case R.id.encryption_choice_pgp:
                if (activity.hasPgp()) {
                    if (conversation.getAccount().getPgpSignature() != null) {
                        updated = conversation.setNextEncryption(Message.ENCRYPTION_PGP);
                        item.setChecked(true);
                    } else {
                        updated = false;
                        activity.announcePgp(conversation.getAccount(), conversation, null, activity.onOpenPGPKeyPublished);
                    }
                } else {
                    activity.showInstallPgpDialog();
                    updated = false;
                }
                break;
            case R.id.encryption_choice_axolotl:
                Log.d(Config.LOGTAG, AxolotlService.getLogprefix(conversation.getAccount())
                        + "Enabled axolotl for Contact " + conversation.getContact().getJid());
                updated = conversation.setNextEncryption(Message.ENCRYPTION_AXOLOTL);
                item.setChecked(true);
                break;
            default:
                updated = conversation.setNextEncryption(Message.ENCRYPTION_NONE);
                break;
        }
        if (updated) {
            activity.xmppConnectionService.updateConversation(conversation);
        }
        updateChatMsgHint();
        getActivity().invalidateOptionsMenu();
        activity.refreshUi();
    }

    public void attachFile(final int attachmentChoice) {
        attachFile(attachmentChoice, true);
    }

    public void attachFile(final int attachmentChoice, final boolean updateRecentlyUsed) {
        if (attachmentChoice == ATTACHMENT_CHOICE_RECORD_VOICE) {
            if (!hasPermissions(attachmentChoice, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)) {
                return;
            }
        } else if (attachmentChoice == ATTACHMENT_CHOICE_TAKE_PHOTO || attachmentChoice == ATTACHMENT_CHOICE_RECORD_VIDEO) {
            if (!hasPermissions(attachmentChoice, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)) {
                return;
            }
        } else if (attachmentChoice == ATTACHMENT_CHOICE_LOCATION) {
            if (!hasPermissions(attachmentChoice, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                return;
            }
        } else if (attachmentChoice == ATTACHMENT_CHOICE_CHOOSE_FILE || attachmentChoice == ATTACHMENT_CHOICE_CHOOSE_IMAGE || attachmentChoice == ATTACHMENT_CHOICE_CHOOSE_VIDEO) {
            if (!hasPermissions(attachmentChoice, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                return;
            }
        }
        if (updateRecentlyUsed) {
            storeRecentlyUsedQuickAction(attachmentChoice);
        }
        final int encryption = conversation.getNextEncryption();
        final int mode = conversation.getMode();
        if (encryption == Message.ENCRYPTION_PGP) {
            if (activity.hasPgp()) {
                if (mode == Conversation.MODE_SINGLE && conversation.getContact().getPgpKeyId() != 0) {
                    activity.xmppConnectionService.getPgpEngine().hasKey(
                            conversation.getContact(),
                            new UiCallback<Contact>() {

                                @Override
                                public void userInputRequired(PendingIntent pi, Contact contact) {
                                    startPendingIntent(pi, attachmentChoice);
                                }

                                @Override
                                public void progress(int progress) {

                                }

                                @Override
                                public void success(Contact contact) {
                                    invokeAttachFileIntent(attachmentChoice);
                                }

                                @Override
                                public void error(int error, Contact contact) {
                                    activity.replaceToast(getString(error));
                                }
                            });
                } else if (mode == Conversation.MODE_MULTI && conversation.getMucOptions().pgpKeysInUse()) {
                    if (!conversation.getMucOptions().everybodyHasKeys()) {
                        getActivity().runOnUiThread(() -> {
                            Toast warning = ToastCompat.makeText(activity, R.string.missing_public_keys, ToastCompat.LENGTH_LONG);
                            warning.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                            warning.show();
                        });
                    }
                    invokeAttachFileIntent(attachmentChoice);
                } else {
                    showNoPGPKeyDialog(false, (dialog, which) -> {
                        conversation.setNextEncryption(Message.ENCRYPTION_NONE);
                        activity.xmppConnectionService.updateConversation(conversation);
                        invokeAttachFileIntent(attachmentChoice);
                    });
                }
            } else {
                activity.showInstallPgpDialog();
            }
        } else {
            invokeAttachFileIntent(attachmentChoice);
        }
    }

    private void storeRecentlyUsedQuickAction(final int attachmentChoice) {
        try {
            activity.getPreferences().edit()
                    .putString(RECENTLY_USED_QUICK_ACTION, SendButtonAction.of(attachmentChoice).toString())
                    .apply();
        } catch (IllegalArgumentException e) {
            //just do not save
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        final PermissionUtils.PermissionResult permissionResult =
                PermissionUtils.removeBluetoothConnect(permissions, grantResults);
        if (grantResults.length > 0) {
            if (allGranted(permissionResult.grantResults)) {
                Activity mXmppActivity = getActivity();
                switch (requestCode) {
                    case REQUEST_START_DOWNLOAD:
                        if (this.mPendingDownloadableMessage != null) {
                            startDownloadable(this.mPendingDownloadableMessage);
                        }
                        break;
                    case REQUEST_ADD_EDITOR_CONTENT:
                        if (this.mPendingEditorContent != null) {
                            attachEditorContentToConversation(this.mPendingEditorContent);
                        }
                        break;
                    case REQUEST_COMMIT_ATTACHMENTS:
                        commitAttachments();
                        break;
                    case REQUEST_START_AUDIO_CALL:
                        if (mXmppActivity instanceof XmppActivity) {
                            CallManager.triggerRtpSession(RtpSessionActivity.ACTION_MAKE_VOICE_CALL, (XmppActivity) mXmppActivity, conversation);
                        }
                        break;
                    case REQUEST_START_VIDEO_CALL:
                        if (mXmppActivity instanceof XmppActivity) {
                            CallManager.triggerRtpSession(RtpSessionActivity.ACTION_MAKE_VIDEO_CALL, (XmppActivity) mXmppActivity, conversation);
                        }
                        break;
                    default:
                        attachFile(requestCode);
                        break;
                }
            } else {
                @StringRes int res;
                String firstDenied = getFirstDenied(grantResults, permissions);
                if (Manifest.permission.RECORD_AUDIO.equals(firstDenied)) {
                    res = R.string.no_microphone_permission;
                } else if (Manifest.permission.CAMERA.equals(firstDenied)) {
                    res = R.string.no_camera_permission;
                } else if (Manifest.permission.ACCESS_COARSE_LOCATION.equals(firstDenied)
                        || Manifest.permission.ACCESS_FINE_LOCATION.equals(firstDenied)) {
                    res = R.string.no_location_permission;
                } else {
                    res = R.string.no_storage_permission;
                }
                ToastCompat.makeText(getActivity(), res, ToastCompat.LENGTH_SHORT).show();
            }
        }
        if (readGranted(grantResults, permissions)) {
            if (activity != null && activity.xmppConnectionService != null) {
                activity.xmppConnectionService.getBitmapCache().evictAll();
                activity.xmppConnectionService.restartFileObserver();
            }
            refresh();
        }
    }

    private void updateChatBG() {
        if (activity != null) {
            if (activity.unicoloredBG()) {
                binding.conversationsFragment.setBackgroundResource(0);
                binding.conversationsFragment.setBackgroundColor(StyledAttributes.getColor(activity, R.attr.color_background_tertiary));
            } else {
                binding.conversationsFragment.setBackground(ContextCompat.getDrawable(activity, R.drawable.chatbg));
            }
        }
    }

    public void startDownloadable(Message message) {
        if (!hasPermissions(REQUEST_START_DOWNLOAD, Manifest.permission.WRITE_EXTERNAL_STORAGE) && !hasPermissions(REQUEST_START_DOWNLOAD, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            this.mPendingDownloadableMessage = message;
            return;
        }
        Transferable transferable = message.getTransferable();
        if (transferable != null) {
            if (transferable instanceof TransferablePlaceholder && message.hasFileOnRemoteHost()) {
                createNewConnection(message);
                return;
            }
            if (!transferable.start()) {
                Log.d(Config.LOGTAG, "type: " + transferable.getClass().getName());
                ToastCompat.makeText(getActivity(), R.string.not_connected_try_again, ToastCompat.LENGTH_SHORT).show();
            }
        } else if (message.treatAsDownloadable() || message.hasFileOnRemoteHost() || MessageUtils.unInitiatedButKnownSize(message)) {
            createNewConnection(message);
        } else {
            Log.d(Config.LOGTAG, message.getConversation().getAccount() + ": unable to start downloadable");
        }
    }

    private void createNewConnection(final Message message) {
        if (!activity.xmppConnectionService.hasInternetConnection()) {
            ToastCompat.makeText(getActivity(), R.string.not_connected_try_again, ToastCompat.LENGTH_SHORT).show();
            return;
        }
        activity.xmppConnectionService.getHttpConnectionManager().createNewDownloadConnection(message, true);
    }

    private OnClickListener OTRwarning = new OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                final Uri uri = Uri.parse("https://monocles.wiki/index.php?title=Monocles_Chat");
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(browserIntent);
            } catch (Exception e) {
                ToastCompat.makeText(activity, R.string.no_application_found_to_open_link, Toast.LENGTH_SHORT).show();
            }
        }
    };


    @SuppressLint("InflateParams")
    protected void clearHistoryDialog(final Conversation conversation) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(getString(R.string.clear_conversation_history));
        final View dialogView = requireActivity().getLayoutInflater().inflate(R.layout.dialog_clear_history, null);
        final CheckBox endConversationCheckBox = dialogView.findViewById(R.id.end_conversation_checkbox);
        if (conversation.getMode() == Conversation.MODE_SINGLE) {
            endConversationCheckBox.setVisibility(View.VISIBLE);
            endConversationCheckBox.setChecked(true);
        }
        builder.setView(dialogView);
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
            this.activity.xmppConnectionService.clearConversationHistory(conversation);
            if (endConversationCheckBox.isChecked()) {
                this.activity.xmppConnectionService.archiveConversation(conversation);
            } else {
                activity.onConversationsListItemUpdated();
                refresh();
            }
        });
        builder.create().show();
    }
    protected void muteConversationDialog(final Conversation conversation) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.disable_notifications);
        final int[] durations = activity.getResources().getIntArray(R.array.mute_options_durations);
        final CharSequence[] labels = new CharSequence[durations.length];
        for (int i = 0; i < durations.length; ++i) {
            if (durations[i] == -1) {
                labels[i] = activity.getString(R.string.until_further_notice);
            } else {
                labels[i] = TimeFrameUtils.resolve(activity, 1000L * durations[i]);
            }
        }
        builder.setItems(labels, (dialog, which) -> {
            final long till;
            if (durations[which] == -1) {
                till = Long.MAX_VALUE;
            } else {
                till = System.currentTimeMillis() + (durations[which] * 1000L);
            }
            conversation.setMutedTill(till);
            activity.xmppConnectionService.updateConversation(conversation);
            activity.onConversationsListItemUpdated();
            refresh();
            activity.invalidateOptionsMenu();
        });
        builder.create().show();
    }

    private boolean hasPermissions(int requestCode, List<String> permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final List<String> missingPermissions = new ArrayList<>();
            for (String permission : permissions) {
                if (Config.ONLY_INTERNAL_STORAGE
                        && permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    continue;
                }
                if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(permission);
                }
            }
            if (missingPermissions.size() == 0) {
                return true;
            } else {
                requestPermissions(
                        missingPermissions.toArray(new String[0]),
                        requestCode);
                return false;
            }
        } else {
            return true;
        }
    }
    private boolean hasPermissions(int requestCode, String... permissions) {
        return hasPermissions(requestCode, ImmutableList.copyOf(permissions));
    }
    public void unmuteConversation(final Conversation conversation) {
        conversation.setMutedTill(0);
        this.activity.xmppConnectionService.updateConversation(conversation);
        this.activity.onConversationsListItemUpdated();
        refresh();
        this.activity.invalidateOptionsMenu();
    }
    protected void invokeAttachFileIntent(final int attachmentChoice) {
        Intent intent = new Intent();
        boolean chooser = false;
        switch (attachmentChoice) {
            case ATTACHMENT_CHOICE_CHOOSE_IMAGE:
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                chooser = true;
                break;
            case ATTACHMENT_CHOICE_CHOOSE_VIDEO:
                chooser = true;
                intent.setType("video/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                break;
            case ATTACHMENT_CHOICE_RECORD_VIDEO:
                if (Compatibility.runsThirty()) {
                    final List<CameraUtils> cameraApps = CameraUtils.getCameraApps(activity);
                    if (cameraApps.size() == 0) {
                        ToastCompat.makeText(activity, R.string.no_application_found, ToastCompat.LENGTH_LONG).show();
                    } else if (cameraApps.size() == 1) {
                        getCameraApp(cameraApps.get(0));
                    } else {
                        if (!activity.getPreferences().contains(SettingsActivity.CAMERA_CHOICE)) {
                            showCameraChooser(activity, cameraApps);
                        } else {
                            intent.setComponent(getCameraApp(cameraApps.get(activity.getPreferences().getInt(SettingsActivity.CAMERA_CHOICE, 0))));
                        }
                    }
                }
                intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
                break;
            case ATTACHMENT_CHOICE_TAKE_PHOTO:
                final Uri photoUri = activity.xmppConnectionService.getFileBackend().getTakePhotoUri();
                pendingTakePhotoUri.push(photoUri);
                if (Compatibility.runsThirty()) {
                    final List<CameraUtils> cameraApps = CameraUtils.getCameraApps(activity);
                    if (cameraApps.size() == 0) {
                        ToastCompat.makeText(activity, R.string.no_application_found, ToastCompat.LENGTH_LONG).show();
                    } else if (cameraApps.size() == 1) {
                        getCameraApp(cameraApps.get(0));
                    } else {
                        if (!activity.getPreferences().contains(SettingsActivity.CAMERA_CHOICE)) {
                            showCameraChooser(activity, cameraApps);
                        } else {
                            intent.setComponent(getCameraApp(cameraApps.get(activity.getPreferences().getInt(SettingsActivity.CAMERA_CHOICE, 0))));
                        }
                    }
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION & Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                break;
            case ATTACHMENT_CHOICE_CHOOSE_FILE:
                chooser = true;
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                break;
            case ATTACHMENT_CHOICE_RECORD_VOICE:
                intent = new Intent(getActivity(), RecordingActivity.class);
                intent.putExtra("ALTERNATIVE_CODEC", activity.xmppConnectionService.alternativeVoiceSettings());
                break;
            case ATTACHMENT_CHOICE_LOCATION:
                intent = GeoHelper.getFetchIntent(activity);
                break;
        }
        final Context context = getActivity();
        if (context == null) {
            return;
        }
        try {
            Log.d(Config.LOGTAG, "Attachment: " + attachmentChoice);
            if (chooser) {
                startActivityForResult(
                        Intent.createChooser(intent, getString(R.string.perform_action_with)),
                        attachmentChoice);
                activity.overridePendingTransition(R.animator.fade_in, R.animator.fade_out);
            } else {
                startActivityForResult(intent, attachmentChoice);
                activity.overridePendingTransition(R.animator.fade_in, R.animator.fade_out);
            }
        } catch (final ActivityNotFoundException e) {

            if (attachmentChoice == ATTACHMENT_CHOICE_RECORD_VIDEO
                    || attachmentChoice == ATTACHMENT_CHOICE_TAKE_PHOTO
                    || attachmentChoice == ATTACHMENT_CHOICE_CHOOSE_FILE
                    || attachmentChoice == ATTACHMENT_CHOICE_CHOOSE_IMAGE
                    || attachmentChoice == ATTACHMENT_CHOICE_CHOOSE_VIDEO){
                ToastCompat.makeText(context, R.string.no_application_found, ToastCompat.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateChatBG();
        disableEncrpytionForExceptions();
        binding.messagesView.post(this::fireReadEvent);
    }
    private void disableEncrpytionForExceptions() {
        if (isEncryptionDisabledException()) {
            disableMessageEncryption();
        }
    }

    private boolean isEncryptionDisabledException() {
        if (conversation != null) {
            return ENCRYPTION_EXCEPTIONS.contains(conversation.getJid().toString());
        }
        return false;
    }
    private void fireReadEvent() {
        if (activity != null && this.conversation != null) {
            String uuid = getLastVisibleMessageUuid();
            if (uuid != null) {
                activity.onConversationRead(this.conversation, uuid);
            }
        }
    }

    private String getLastVisibleMessageUuid() {
        if (binding == null) {
            return null;
        }
        synchronized (this.messageList) {
            int pos = binding.messagesView.getLastVisiblePosition();
            if (pos >= 0) {
                Message message = null;
                for (int i = pos; i >= 0; --i) {
                    try {
                        message = (Message) binding.messagesView.getItemAtPosition(i);
                    } catch (IndexOutOfBoundsException e) {
                        //should not happen if we synchronize properly. however if that fails we just gonna try item -1
                        continue;
                    }
                    if (message.getType() != Message.TYPE_STATUS) {
                        break;
                    }
                }
                if (message != null) {
                    while (message.next() != null && message.next().wasMergedIntoPrevious()) {
                        message = message.next();
                    }
                    return message.getUuid();
                }
            }
        }
        return null;
    }

    private void openWith(final Message message) {
        if (message.isGeoUri()) {
            GeoHelper.view(getActivity(), message);
        } else {
            final DownloadableFile file = activity.xmppConnectionService.getFileBackend().getFile(message);
            ViewUtil.view(activity, file);
        }
    }

    private void showErrorMessage(final Message message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.error_message);
        final String errorMessage = message.getErrorMessage();
        final String[] errorMessageParts = errorMessage == null ? new String[0] : errorMessage.split("\\u001f");
        final String displayError;
        if (errorMessageParts.length == 2) {
            displayError = errorMessageParts[1];
        } else {
            displayError = errorMessage;
        }
        builder.setMessage(displayError);
        builder.setNegativeButton(R.string.copy_to_clipboard, (dialog, which) -> {
            activity.copyTextToClipboard(displayError, R.string.error_message);
            ToastCompat.makeText(activity, R.string.error_message_copied_to_clipboard, ToastCompat.LENGTH_SHORT).show();
        });
        builder.setPositiveButton(R.string.ok, null);
        builder.create().show();
    }

    private void deleteMessage(final Message message) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setNegativeButton(R.string.cancel, null);
        builder.setTitle(R.string.delete_message_dialog);
        builder.setMessage(R.string.delete_message_dialog_msg);

        final Message finalMessage = message;

        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {

            if (finalMessage.getType() == Message.TYPE_TEXT
                    && !finalMessage.isGeoUri()
                    && finalMessage.getConversation() instanceof Conversation) {

                Message retractedMessage = finalMessage;
                retractedMessage.setMessageDeleted(true);

                long time = System.currentTimeMillis();
                Message retractmessage = new Message(conversation,
                        "This person attempted to retract a previous message, but it's unsupported by your client.",
                        Message.ENCRYPTION_NONE,
                        Message.STATUS_SEND);
                if (retractedMessage.getEditedList().size() > 0) {
                    retractmessage.setRetractId(retractedMessage.getEditedList().get(0).getEditedId());
                } else {
                    retractmessage.setRetractId(retractedMessage.getRemoteMsgId() != null ? retractedMessage.getRemoteMsgId() : retractedMessage.getUuid());
                }

                retractedMessage.putEdited(retractedMessage.getUuid(), retractedMessage.getServerMsgId(), retractedMessage.getBody(), retractedMessage.getTimeSent());
                retractedMessage.setBody(Message.DELETED_MESSAGE_BODY);
                retractedMessage.setServerMsgId(null);
                retractedMessage.setRemoteMsgId(message.getRemoteMsgId());
                retractedMessage.setMessageDeleted(true);

                retractmessage.setType(Message.TYPE_TEXT);
                retractmessage.setCounterpart(message.getCounterpart());
                retractmessage.setTrueCounterpart(message.getTrueCounterpart());
                retractmessage.setTime(time);
                retractmessage.setUuid(UUID.randomUUID().toString());
                retractmessage.setCarbon(false);
                retractmessage.setOob(String.valueOf(false));
                retractmessage.setRemoteMsgId(retractmessage.getUuid());
                retractmessage.setMessageDeleted(true);
                retractedMessage.setTime(time); //set new time here to keep orginal timestamps
                for (Edit itm : retractedMessage.getEditedList()) {
                    Message tmpRetractedMessage = conversation.findMessageWithUuidOrRemoteId(itm.getEditedId());
                    if (tmpRetractedMessage != null) {
                        tmpRetractedMessage.setMessageDeleted(true);
                        activity.xmppConnectionService.updateMessage(tmpRetractedMessage, tmpRetractedMessage.getUuid());
                    }
                }
                activity.xmppConnectionService.updateMessage(retractedMessage, retractedMessage.getUuid());
                if (finalMessage.getStatus() >= Message.STATUS_SEND) {
                    //only send retraction messages vor outgoing messages!
                    sendMessage(retractmessage);
                }
                activity.xmppConnectionService.deleteMessage(conversation, retractedMessage);
                activity.xmppConnectionService.deleteMessage(conversation, retractmessage);
            }
            activity.xmppConnectionService.deleteMessage(conversation, message);
            activity.onConversationsListItemUpdated();
            refresh();
        });
        builder.create().show();
    }

    private void deleteFile(final Message message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setNegativeButton(R.string.cancel, null);
        builder.setTitle(R.string.delete_file_dialog);
        builder.setMessage(R.string.delete_file_dialog_msg);
        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            if (activity.xmppConnectionService.getFileBackend().deleteFile(message)) {
                message.setFileDeleted(true);
                activity.xmppConnectionService.evictPreview(message.getUuid());
                activity.xmppConnectionService.updateMessage(message, false);
                activity.onConversationsListItemUpdated();
                refresh();
            }
        });
        builder.create().show();
    }

    public void resendMessage(final Message message) {
        if (message != null && message.isFileOrImage()) {
            if (!(message.getConversation() instanceof Conversation)) {
                return;
            }
            final Conversation conversation = (Conversation) message.getConversation();
            final DownloadableFile file = activity.xmppConnectionService.getFileBackend().getFile(message);
            if ((file.exists() && file.canRead()) || message.hasFileOnRemoteHost()) {
                final XmppConnection xmppConnection = conversation.getAccount().getXmppConnection();
                if (!message.hasFileOnRemoteHost()
                        && xmppConnection != null
                        && conversation.getMode() == Conversational.MODE_SINGLE
                        && !xmppConnection.getFeatures().httpUpload(message.getFileParams().getSize())) {
                    activity.selectPresence(conversation, () -> {
                        message.setCounterpart(conversation.getNextCounterpart());
                        activity.xmppConnectionService.resendFailedMessages(message);
                        new Handler().post(() -> {
                            int size = messageList.size();
                            this.binding.messagesView.setSelection(size - 1);
                        });
                    });
                    return;
                }
            } else if (!Compatibility.hasStoragePermission(getActivity())) {
                ToastCompat.makeText(activity, R.string.no_storage_permission, ToastCompat.LENGTH_SHORT).show();
                return;
            } else {
                ToastCompat.makeText(activity, R.string.file_deleted, ToastCompat.LENGTH_SHORT).show();
                message.setFileDeleted(true);
                activity.xmppConnectionService.updateMessage(message, false);
                activity.onConversationsListItemUpdated();
                refresh();
                return;
            }
        }
        activity.xmppConnectionService.resendFailedMessages(message);
        new Handler().post(() -> {
            int size = messageList.size();
            this.binding.messagesView.setSelection(size - 1);
        });
    }

    private void copyUrl(Message message) {
        final String url;
        final int resId;
        if (message.isGeoUri()) {
            resId = R.string.location;
            url = message.getBody();
        } else if (message.isXmppUri()) {
            resId = R.string.contact;
            url = message.getBody();
        } else if (message.hasFileOnRemoteHost()) {
            resId = R.string.file_url;
            url = message.getFileParams().url.toString();
        } else {
            url = message.getBody().trim();
            resId = R.string.file_url;
        }
        if (activity.copyTextToClipboard(url, resId)) {
            ToastCompat.makeText(getActivity(), R.string.url_copied_to_clipboard,
                    ToastCompat.LENGTH_SHORT).show();
        }
    }

    public void cancelTransmission(Message message) {
        Transferable transferable = message.getTransferable();
        if (transferable != null) {
            transferable.cancel();
        } else if (message.getStatus() != Message.STATUS_RECEIVED) {
            activity.xmppConnectionService.markMessage(message, Message.STATUS_SEND_FAILED, Message.ERROR_MESSAGE_CANCELLED);
        }
    }

    private void retryDecryption(Message message) {
        message.setEncryption(Message.ENCRYPTION_PGP);
        activity.onConversationsListItemUpdated();
        refresh();
        conversation.getAccount().getPgpDecryptionService().decrypt(message, false);
    }

    public void privateMessageWith(final Jid counterpart) {
        try {
            final Jid tcp = conversation.getMucOptions().getTrueCounterpart(counterpart);
            if (!getConversation().getMucOptions().isUserInRoom(counterpart) && getConversation().getMucOptions().findUserByRealJid(tcp == null ? null : tcp.asBareJid()) == null) {
                ToastCompat.makeText(getActivity(), activity.getString(R.string.user_has_left_conference, counterpart.getResource()), ToastCompat.LENGTH_SHORT).show();
                return;
            }
            if (conversation.setOutgoingChatState(Config.DEFAULT_CHAT_STATE)) {
                activity.xmppConnectionService.sendChatState(conversation);
            }
            this.binding.textinput.setText("");
            this.conversation.setNextCounterpart(counterpart);
        } catch (Exception e) {
            e.printStackTrace();
            ToastCompat.makeText(getActivity(), activity.getString(R.string.user_has_left_conference, activity.getString(R.string.user)), ToastCompat.LENGTH_SHORT).show();
        } finally {
            updateChatMsgHint();
            updateSendButton();
            updateEditablity();
        }
    }

    private void correctMessage(Message message) {
        while (message.mergeable(message.next())) {
            message = message.next();
        }
        this.conversation.setCorrectingMessage(message);
        final Editable editable = binding.textinput.getText();
        this.conversation.setDraftMessage(editable.toString());
        this.binding.textinput.setText("");
        this.binding.textinput.append(message.getBody());

    }

    private void highlightInConference(String nick) {
        final Editable editable = this.binding.textinput.getText();
        String oldString = editable.toString().trim();
        final int pos = this.binding.textinput.getSelectionStart();
        if (oldString.isEmpty() || pos == 0) {
            editable.insert(0, nick + ": ");
        } else {
            final char before = editable.charAt(pos - 1);
            final char after = editable.length() > pos ? editable.charAt(pos) : '\0';
            if (before == '\n') {
                editable.insert(pos, nick + ": ");
            } else {
                if (pos > 2 && editable.subSequence(pos - 2, pos).toString().equals(": ")) {
                    if (NickValidityChecker.check(conversation, Arrays.asList(editable.subSequence(0, pos - 2).toString().split(", ")))) {
                        editable.insert(pos - 2, ", " + nick);
                        return;
                    }
                }
                editable.insert(pos, (Character.isWhitespace(before) ? "" : " ") + nick + (Character.isWhitespace(after) ? "" : " "));
                if (Character.isWhitespace(after)) {
                    this.binding.textinput.setSelection(this.binding.textinput.getSelectionStart() + 1);
                }
            }
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        final Activity activity = getActivity();
        if (activity instanceof ConversationsActivity) {
            ((ConversationsActivity) activity).clearPendingViewIntent();
        }
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (conversation != null) {
            outState.putString(STATE_CONVERSATION_UUID, conversation.getUuid());
            outState.putString(STATE_LAST_MESSAGE_UUID, lastMessageUuid);
            final Uri uri = pendingTakePhotoUri.peek();
            if (uri != null) {
                outState.putString(STATE_PHOTO_URI, uri.toString());
            }
            final ScrollState scrollState = getScrollPosition();
            if (scrollState != null) {
                outState.putParcelable(STATE_SCROLL_POSITION, scrollState);
            }
            final ArrayList<Attachment> attachments = mediaPreviewAdapter == null ? new ArrayList<>() : mediaPreviewAdapter.getAttachments();
            if (attachments.size() > 0) {
                outState.putParcelableArrayList(STATE_MEDIA_PREVIEWS, attachments);
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }
        String uuid = savedInstanceState.getString(STATE_CONVERSATION_UUID);
        ArrayList<Attachment> attachments = savedInstanceState.getParcelableArrayList(STATE_MEDIA_PREVIEWS);
        pendingLastMessageUuid.push(savedInstanceState.getString(STATE_LAST_MESSAGE_UUID, null));
        if (uuid != null) {
            QuickLoader.set(uuid);
            this.pendingConversationsUuid.push(uuid);
            if (attachments != null && attachments.size() > 0) {
                this.pendingMediaPreviews.push(attachments);
            }
            String takePhotoUri = savedInstanceState.getString(STATE_PHOTO_URI);
            if (takePhotoUri != null) {
                pendingTakePhotoUri.push(Uri.parse(takePhotoUri));
            }
            pendingScrollState.push(savedInstanceState.getParcelable(STATE_SCROLL_POSITION));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateChatBG();
        disableEncrpytionForExceptions();
        if (this.reInitRequiredOnStart && this.conversation != null) {
            final Bundle extras = pendingExtras.pop();
            reInit(this.conversation, extras != null);
            if (extras != null) {
                processExtras(extras);
            }
        } else if (conversation == null && activity != null && activity.xmppConnectionService != null) {
            final String uuid = pendingConversationsUuid.pop();
            Log.d(Config.LOGTAG, "ConversationFragment.onStart() - activity was bound but no conversation loaded. uuid=" + uuid);
            if (uuid != null) {
                findAndReInitByUuidOrArchive(uuid);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        final Activity activity = getActivity();
        messageListAdapter.unregisterListenerInAudioPlayer();
        if (activity == null || !activity.isChangingConfigurations()) {
            hideSoftKeyboard(activity);
            messageListAdapter.stopAudioPlayer();
        }
        if (this.conversation != null) {
            final String msg = this.binding.textinput.getText().toString();
            storeNextMessage(msg);
            updateChatState(this.conversation, msg);
            this.activity.xmppConnectionService.getNotificationService().setOpenConversation(null);
        }
        this.reInitRequiredOnStart = true;
    }

    private void updateChatState(final Conversation conversation, final String msg) {
        ChatState state = msg.length() == 0 ? Config.DEFAULT_CHAT_STATE : ChatState.PAUSED;
        Account.State status = conversation.getAccount().getStatus();
        if (status == Account.State.ONLINE && conversation.setOutgoingChatState(state)) {
            activity.xmppConnectionService.sendChatState(conversation);
        }
    }

    private void saveMessageDraftStopAudioPlayer() {
        final Conversation previousConversation = this.conversation;
        if (this.activity == null || this.binding == null || previousConversation == null) {
            return;
        }
        Log.d(Config.LOGTAG, "ConversationFragment.saveMessageDraftStopAudioPlayer()");
        final String msg = this.binding.textinput.getText().toString();
        storeNextMessage(msg);
        updateChatState(this.conversation, msg);
        messageListAdapter.stopAudioPlayer();
        mediaPreviewAdapter.clearPreviews();
        toggleInputMethod();
    }

    public void reInit(final Conversation conversation, final Bundle extras) {
        QuickLoader.set(conversation.getUuid());
        final boolean changedConversation = this.conversation != conversation;
        if (changedConversation) {
            this.saveMessageDraftStopAudioPlayer();
        }
        this.clearPending();
        if (this.reInit(conversation, extras != null)) {
            if (extras != null) {
                processExtras(extras);
            }
            this.reInitRequiredOnStart = false;
        } else {
            this.reInitRequiredOnStart = true;
            pendingExtras.push(extras);
        }
        resetUnreadMessagesCount();
    }

    private void reInit(Conversation conversation) {
        reInit(conversation, false);
    }

    private boolean reInit(final Conversation conversation, final boolean hasExtras) {
        if (conversation == null) {
            return false;
        }
        this.conversation = conversation;
        //once we set the conversation all is good and it will automatically do the right thing in onStart()
        if (this.activity == null || this.binding == null) {
            return false;
        }

        if (!activity.xmppConnectionService.isConversationStillOpen(this.conversation)) {
            activity.onConversationArchived(this.conversation);
            return false;
        }

        stopScrolling();
        Log.d(Config.LOGTAG, "reInit(hasExtras=" + Boolean.toString(hasExtras) + ")");

        if (this.conversation.isRead() && hasExtras) {
            Log.d(Config.LOGTAG, "trimming conversation");
            this.conversation.trim();
        }

        setupIme();

        final boolean scrolledToBottomAndNoPending = this.scrolledToBottom() && pendingScrollState.peek() == null;

        this.binding.textSendButton.setContentDescription(activity.getString(R.string.send_message_to_x, conversation.getName()));
        this.binding.textinput.setKeyboardListener(null);
        showRecordVoiceButton();
        final boolean participating = conversation.getMode() == Conversational.MODE_SINGLE || conversation.getMucOptions().participating();
        if (participating) {
            this.binding.textinput.setText(this.conversation.getNextMessage());
            this.binding.textinput.setSelection(this.binding.textinput.length());
        } else {
            this.binding.textinput.setText(MessageUtils.EMPTY_STRING);
        }
        this.binding.textinput.setKeyboardListener(this);
        messageListAdapter.updatePreferences();
        refresh(false);
        activity.invalidateOptionsMenu();
        this.conversation.messagesLoaded.set(true);
        hasWriteAccessInMUC();
        Log.d(Config.LOGTAG, "scrolledToBottomAndNoPending=" + Boolean.toString(scrolledToBottomAndNoPending));

        if (hasExtras || scrolledToBottomAndNoPending) {
            resetUnreadMessagesCount();
            synchronized (this.messageList) {
                Log.d(Config.LOGTAG, "jump to first unread message");
                final Message first = conversation.getFirstUnreadMessage();
                final int bottom = Math.max(0, this.messageList.size() - 1);
                final int pos;
                final boolean jumpToBottom;
                if (first == null) {
                    pos = bottom;
                    jumpToBottom = true;
                } else {
                    int i = getIndexOf(first.getUuid(), this.messageList);
                    pos = i < 0 ? bottom : i;
                    jumpToBottom = false;
                }
                setSelection(pos, jumpToBottom);
            }
        }

        this.binding.messagesView.post(this::fireReadEvent);
        //TODO if we only do this when this fragment is running on main it won't *bing* in tablet layout which might be unnecessary since we can *see* it
        activity.xmppConnectionService.getNotificationService().setOpenConversation(this.conversation);
        if (commandAdapter == null && conversation != null) {
            conversation.setupViewPager(binding.conversationViewPager, binding.tabLayout);
            commandAdapter = new CommandAdapter((XmppActivity) getActivity());
            binding.commandsView.setAdapter(commandAdapter);
            binding.commandsView.setOnItemClickListener((parent, view, position, id) -> {
                conversation.startCommand(commandAdapter.getItem(position), activity.xmppConnectionService);
            });
            refreshCommands();
        }

        return true;
    }

    private void hasWriteAccessInMUC() {
        if ((conversation.getMode() == Conversation.MODE_MULTI && !conversation.getMucOptions().participating()) && !activity.xmppConnectionService.hideYouAreNotParticipating()) {
            activity.runOnUiThread(() -> {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.you_are_not_participating));
                builder.setMessage(getString(R.string.no_write_access_in_public_muc));
                builder.setNegativeButton(getString(R.string.hide_warning),
                        (dialog, which) -> {
                            SharedPreferences preferences = activity.getPreferences();
                            preferences.edit().putBoolean(HIDE_YOU_ARE_NOT_PARTICIPATING, true).apply();
                            hideSnackbar();
                        });
                builder.setPositiveButton(getString(R.string.ok),
                        (dialog, which) -> {
                            try {
                                Intent intent = new Intent(getActivity(), ConferenceDetailsActivity.class);
                                intent.setAction(ConferenceDetailsActivity.ACTION_VIEW_MUC);
                                intent.putExtra("uuid", conversation.getUuid());
                                startActivity(intent);
                                activity.overridePendingTransition(R.animator.fade_in, R.animator.fade_out);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                builder.create().show();
            });
            showSnackbar(R.string.no_write_access_in_public_muc, R.string.ok, clickToMuc);
        }
    }
    protected void refreshCommands() {
        if (commandAdapter == null) return;

        Jid commandJid = conversation.getContact().resourceWhichSupport(Namespace.COMMANDS);
        if (commandJid == null) {
            conversation.hideViewPager();
        } else {
            conversation.showViewPager();
            activity.xmppConnectionService.fetchCommands(conversation.getAccount(), commandJid, (a, iq) -> {
                if (activity == null) return;

                activity.runOnUiThread(() -> {
                    if (iq.getType() == IqPacket.TYPE.RESULT) {
                        binding.commandsViewProgressbar.setVisibility(View.GONE);
                        commandAdapter.clear();
                        for (Element child : iq.query().getChildren()) {
                            if (!"item".equals(child.getName()) || !Namespace.DISCO_ITEMS.equals(child.getNamespace())) continue;
                            commandAdapter.add(child);
                        }
                    }

                    if (commandAdapter.getCount() < 1) conversation.hideViewPager();
                });
            });
        }
    }

    private void resetUnreadMessagesCount() {
        lastMessageUuid = null;
        hideUnreadMessagesCount();
    }

    private void hideUnreadMessagesCount() {
        if (this.binding == null) {
            return;
        }
        this.binding.scrollToBottomButton.setEnabled(false);
        this.binding.scrollToBottomButton.hide();
        this.binding.unreadCountCustomView.setVisibility(View.GONE);
    }

    private void setSelection(int pos, boolean jumpToBottom) {
        ListViewUtils.setSelection(this.binding.messagesView, pos, jumpToBottom);
        this.binding.messagesView.post(() -> ListViewUtils.setSelection(this.binding.messagesView, pos, jumpToBottom));
        this.binding.messagesView.post(this::fireReadEvent);
    }

    private boolean scrolledToBottom() {
        return this.binding != null && scrolledToBottom(this.binding.messagesView);
    }

    private void processExtras(final Bundle extras) {
        final String downloadUuid = extras.getString(ConversationsActivity.EXTRA_DOWNLOAD_UUID);
        final String text = extras.getString(Intent.EXTRA_TEXT);
        final String nick = extras.getString(ConversationsActivity.EXTRA_NICK);
        final String postInitAction = extras.getString(ConversationsActivity.EXTRA_POST_INIT_ACTION);
        final boolean asQuote = extras.getBoolean(ConversationsActivity.EXTRA_AS_QUOTE);
        final String user = extras.getString(ConversationsActivity.EXTRA_USER);
        final boolean pm = extras.getBoolean(ConversationsActivity.EXTRA_IS_PRIVATE_MESSAGE, false);
        final boolean doNotAppend = extras.getBoolean(ConversationsActivity.EXTRA_DO_NOT_APPEND, false);
        final String type = extras.getString(ConversationsActivity.EXTRA_TYPE);
        final List<Uri> uris = extractUris(extras);
        if (uris != null && uris.size() > 0) {
            if (uris.size() == 1 && "geo".equals(uris.get(0).getScheme())) {
                mediaPreviewAdapter.addMediaPreviews(Attachment.of(getActivity(), uris.get(0), Attachment.Type.LOCATION));
            } else {
                final List<Uri> cleanedUris = cleanUris(new ArrayList<>(uris));
                mediaPreviewAdapter.addMediaPreviews(Attachment.of(getActivity(), cleanedUris, type));
            }
            toggleInputMethod();
            return;
        }
        if (nick != null) {
            if (pm) {
                Jid jid = conversation.getJid();
                try {
                    Jid next = Jid.of(jid.getLocal(), jid.getDomain(), nick);
                    privateMessageWith(next);
                } catch (final IllegalArgumentException ignored) {
                    //do nothing
                }
            } else {
                final MucOptions mucOptions = conversation.getMucOptions();
                if (mucOptions.participating() || conversation.getNextCounterpart() != null) {
                    highlightInConference(nick);
                }
            }
        } else {
            if (text != null && GeoHelper.GEO_URI.matcher(text).matches()) {
                mediaPreviewAdapter.addMediaPreviews(Attachment.of(getActivity(), Uri.parse(text), Attachment.Type.LOCATION));
                toggleInputMethod();
                return;
            } else if (text != null && asQuote) {
                quoteText(text, user);
            } else {
                appendText(text, doNotAppend);
            }
        }
        if (ConversationsActivity.POST_ACTION_RECORD_VOICE.equals(postInitAction)) {
            attachFile(ATTACHMENT_CHOICE_RECORD_VOICE, false);
            return;
        }
        final Message message = downloadUuid == null ? null : conversation.findMessageWithFileAndUuid(downloadUuid);
        if (message != null) {
            startDownloadable(message);
        }
    }

    private List<Uri> extractUris(final Bundle extras) {
        final List<Uri> uris = extras.getParcelableArrayList(Intent.EXTRA_STREAM);
        if (uris != null) {
            return uris;
        }
        final Uri uri = extras.getParcelable(Intent.EXTRA_STREAM);
        if (uri != null) {
            return Collections.singletonList(uri);
        } else {
            return null;
        }
    }

    private List<Uri> cleanUris(final List<Uri> uris) {
        final Iterator<Uri> iterator = uris.iterator();
        while (iterator.hasNext()) {
            final Uri uri = iterator.next();
            if (FileBackend.weOwnFile(uri)) {
                iterator.remove();
                ToastCompat.makeText(getActivity(), R.string.security_violation_not_attaching_file, ToastCompat.LENGTH_SHORT).show();
            }
        }
        return uris;
    }

    private boolean showBlockSubmenu(View view) {
        final Jid jid = conversation.getJid();
        final boolean showReject = conversation.getContact().getOption(Contact.Options.PENDING_SUBSCRIPTION_REQUEST);
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        popupMenu.inflate(R.menu.block);
        popupMenu.getMenu().findItem(R.id.block_contact).setVisible(jid.getLocal() != null);
        popupMenu.getMenu().findItem(R.id.reject).setVisible(showReject);
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            Blockable blockable;
            switch (menuItem.getItemId()) {
                case R.id.reject:
                    activity.xmppConnectionService.stopPresenceUpdatesTo(conversation.getContact());
                    updateSnackBar(conversation);
                    return true;
                case R.id.block_domain:
                    blockable = conversation.getAccount().getRoster().getContact(jid.getDomain());
                    break;
                default:
                    blockable = conversation;
            }
            BlockContactDialog.show(activity, blockable);
            return true;
        });
        popupMenu.show();
        return true;
    }

    @SuppressLint("StringFormatInvalid")
    private void updateSnackBar(final Conversation conversation) {
        if (conversation == null) {
            return;
        }
        final Account account = conversation.getAccount();
        final XmppConnection connection = account.getXmppConnection();
        final int mode = conversation.getMode();
        final Contact contact = mode == Conversation.MODE_SINGLE ? conversation.getContact() : null;
        if (conversation.getStatus() == Conversation.STATUS_ARCHIVED) {
            return;
        }
        if (account.getStatus() == Account.State.DISABLED) {
            showSnackbar(R.string.this_account_is_disabled, R.string.enable, this.mEnableAccountListener);
        } else if (conversation.isBlocked()) {
            showSnackbar(R.string.contact_blocked, R.string.unblock, this.mUnblockClickListener);
        } else if (contact != null && !contact.showInRoster() && contact.getOption(Contact.Options.PENDING_SUBSCRIPTION_REQUEST)) {
            showSnackbar(R.string.contact_added_you, R.string.add_back, this.mAddBackClickListener, this.mLongPressBlockListener);
        } else if (contact != null && contact.getOption(Contact.Options.PENDING_SUBSCRIPTION_REQUEST)) {
            showSnackbar(R.string.contact_asks_for_presence_subscription, R.string.allow, this.mAllowPresenceSubscription, this.mLongPressBlockListener);
        } else if (mode == Conversation.MODE_MULTI
                && !conversation.getMucOptions().online()
                && account.getStatus() == Account.State.ONLINE) {
            switch (conversation.getMucOptions().getError()) {
                case NICK_IN_USE:
                    showSnackbar(R.string.nick_in_use, R.string.edit, clickToMuc);
                    break;
                case NO_RESPONSE:
                    showSnackbar(R.string.joining_conference, 0, null);
                    break;
                case SERVER_NOT_FOUND:
                    if (conversation.receivedMessagesCount() > 0) {
                        showSnackbar(R.string.remote_server_not_found, R.string.try_again, joinMuc);
                    } else {
                        showSnackbar(R.string.remote_server_not_found, R.string.leave, leaveMuc);
                    }
                    break;
                case REMOTE_SERVER_TIMEOUT:
                    if (conversation.receivedMessagesCount() > 0) {
                        showSnackbar(R.string.remote_server_timeout, R.string.try_again, joinMuc);
                    } else {
                        showSnackbar(R.string.remote_server_timeout, R.string.leave, leaveMuc);
                    }
                    break;
                case PASSWORD_REQUIRED:
                    showSnackbar(R.string.conference_requires_password, R.string.enter_password, enterPassword);
                    break;
                case BANNED:
                    showSnackbar(R.string.conference_banned, R.string.leave, leaveMuc);
                    break;
                case MEMBERS_ONLY:
                    showSnackbar(R.string.conference_members_only, R.string.leave, leaveMuc);
                    break;
                case RESOURCE_CONSTRAINT:
                    showSnackbar(R.string.conference_resource_constraint, R.string.try_again, joinMuc);
                    break;
                case KICKED:
                    showSnackbar(R.string.conference_kicked, R.string.join, joinMuc);
                    break;
                case TECHNICAL_PROBLEMS:
                    showSnackbar(R.string.conference_technical_problems, R.string.try_again, joinMuc);
                    break;
                case UNKNOWN:
                    showSnackbar(R.string.conference_unknown_error, R.string.join, joinMuc);
                    break;
                case INVALID_NICK:
                    showSnackbar(R.string.invalid_muc_nick, R.string.edit, clickToMuc);
                case SHUTDOWN:
                    showSnackbar(R.string.conference_shutdown, R.string.try_again, joinMuc);
                    break;
                case DESTROYED:
                    showSnackbar(R.string.conference_destroyed, R.string.leave, leaveMuc);
                    break;
                case NON_ANONYMOUS:
                    showSnackbar(R.string.group_chat_will_make_your_jabber_id_public, R.string.join, acceptJoin);
                    break;
                default:
                    hideSnackbar();
                    break;
            }
        } else if (account.hasPendingPgpIntent(conversation)) {
            showSnackbar(R.string.openpgp_messages_found, R.string.decrypt, clickToDecryptListener);
        } else if (mode == Conversation.MODE_SINGLE
                && conversation.smpRequested()) {
            showSnackbar(R.string.smp_requested, R.string.verify, this.mAnswerSmpClickListener);
        } else if (mode == Conversation.MODE_SINGLE
                && conversation.hasValidOtrSession()
                && (conversation.getOtrSession().getSessionStatus() == SessionStatus.ENCRYPTED)
                && (!conversation.isOtrFingerprintVerified())) {
            showSnackbar(R.string.unknown_otr_fingerprint, R.string.verify, clickToVerify);
        } else if (connection != null
                && connection.getFeatures().blocking()
                && conversation.countMessages() != 0
                && !conversation.isBlocked()
                && conversation.isWithStranger()) {
            showSnackbar(R.string.received_message_from_stranger, R.string.block, mBlockClickListener);
        } else if (activity != null && activity.warnUnecryptedChat()) {
            if (conversation.getNextEncryption() == Message.ENCRYPTION_NONE && conversation.isSingleOrPrivateAndNonAnonymous() && ((Config.supportOmemo() && Conversation.suitableForOmemoByDefault(conversation)) ||
                    (Config.supportOpenPgp() && account.isPgpDecryptionServiceConnected()) || (
                    mode == Conversation.MODE_SINGLE && Config.supportOtr()))) {
                if (isEncryptionDisabledException() || conversation.getJid().toString().equals(account.getJid().getDomain())) {
                    hideSnackbar();
                } else {
                    showSnackbar(R.string.conversation_unencrypted_hint, R.string.ok, showUnencryptionHintDialog);
                }
            } else {
                hideSnackbar();
            }
        } else if (conversation.getUuid().equalsIgnoreCase(AttachFileToConversationRunnable.isCompressingVideo[0])) {
            Activity activity = getActivity();
            if (activity != null) {
                showSnackbar(getString(R.string.transcoding_video_x, AttachFileToConversationRunnable.isCompressingVideo[1]), 0, null);
            }
        } else {
            hideSnackbar();
        }
    }

    private OnClickListener showUnencryptionHintDialog = new OnClickListener() {
        @Override
        public void onClick(View v) {
            activity.runOnUiThread(() -> {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.message_encryption));
                builder.setMessage(getString(R.string.enable_message_encryption));
                builder.setNegativeButton(getString(R.string.cancel), null);
                builder.setPositiveButton(getString(R.string.enable),
                        (dialog, which) -> {
                            enableMessageEncryption();
                        });
                builder.setNeutralButton(getString(R.string.hide_warning),
                        (dialog, which) -> {
                            SharedPreferences preferences = activity.getPreferences();
                            preferences.edit().putBoolean(WARN_UNENCRYPTED_CHAT, false).apply();
                            hideSnackbar();
                        });
                builder.create().show();
            });
        }
    };

    @Override
    public void refresh() {
        if (this.binding == null) {
            Log.d(Config.LOGTAG, "ConversationFragment.refresh() skipped updated because view binding was null");
            return;
        }
        updateChatBG();
        disableEncrpytionForExceptions();
        if (this.conversation != null && this.activity != null && this.activity.xmppConnectionService != null) {
            if (!activity.xmppConnectionService.isConversationStillOpen(this.conversation)) {
                activity.onConversationArchived(this.conversation);
                return;
            }
        }
        this.refresh(true);
    }


    private void refresh(boolean notifyConversationRead) {
        synchronized (this.messageList) {
            if (this.conversation != null) {
                conversation.populateWithMessages(ConversationFragment.this.messageList);
                updateStatusMessages();
                if (conversation.unreadCount() > 0) {
                    binding.unreadCountCustomView.setVisibility(View.VISIBLE);
                    binding.unreadCountCustomView.setUnreadCount(conversation.unreadCount());
                }
                this.messageListAdapter.notifyDataSetChanged();
                updateChatMsgHint();
                if (notifyConversationRead && activity != null) {
                    binding.messagesView.post(this::fireReadEvent);
                }
                updateSendButton();
                updateEditablity();
            }
        }
    }

    protected void messageSent() {
        mSendingPgpMessage.set(false);
        this.binding.textinput.setText("");
        if (conversation.setCorrectingMessage(null)) {
            this.binding.textinput.append(conversation.getDraftMessage());
            conversation.setDraftMessage(null);
        }
        storeNextMessage();
        updateChatMsgHint();
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(activity);
        final boolean prefScrollToBottom = p.getBoolean("scroll_to_bottom", activity.getResources().getBoolean(R.bool.scroll_to_bottom));
        if (prefScrollToBottom || scrolledToBottom()) {
            new Handler().post(() -> {
                int size = messageList.size();
                this.binding.messagesView.setSelection(size - 1);
            });
        }
    }

    private boolean storeNextMessage() {
        return storeNextMessage(this.binding.textinput.getText().toString());
    }

    private boolean storeNextMessage(String msg) {
        final boolean participating = conversation.getMode() == Conversational.MODE_SINGLE || conversation.getMucOptions().participating();
        if (this.conversation.getStatus() != Conversation.STATUS_ARCHIVED && participating && this.conversation.setNextMessage(msg)) {
            this.activity.xmppConnectionService.updateConversation(this.conversation);
            return true;
        }
        return false;
    }

    public void doneSendingPgpMessage() {
        mSendingPgpMessage.set(false);
    }

    public long getMaxHttpUploadSize(Conversation conversation) {
        final XmppConnection connection = conversation.getAccount().getXmppConnection();
        return connection == null ? -1 : connection.getFeatures().getMaxHttpUploadSize();
    }

    private void updateTextFormat(final boolean me) {
        KeyboardUtils.addKeyboardToggleListener(activity, isVisible -> {
            Log.d(Config.LOGTAG, "keyboard visible: " + isVisible);
            if (isVisible && activity != null && activity.xmppConnectionService != null && activity.xmppConnectionService.showTextFormatting()) {
                showTextFormat(me);
            } else {
                hideTextFormat();
            }
        });
    }

    private void updateEditablity() {
        boolean canWrite = this.conversation.getMode() == Conversation.MODE_SINGLE || this.conversation.getMucOptions().participating() || this.conversation.getNextCounterpart() != null;
        this.binding.textinput.setFocusable(canWrite);
        this.binding.textinput.setFocusableInTouchMode(canWrite);
        this.binding.textSendButton.setEnabled(canWrite);
        this.binding.textinput.setCursorVisible(canWrite);
        this.binding.textinput.setEnabled(canWrite);
    }

    public void updateSendButton() {
        messageListAdapter.setBubbleBackgroundColor(binding.messageInputBox, 0, isPrivateMessage(), true);
        boolean hasAttachments = mediaPreviewAdapter != null && mediaPreviewAdapter.hasAttachments();
        boolean useSendButtonToIndicateStatus = activity != null && PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("send_button_status", getResources().getBoolean(R.bool.send_button_status));
        final Conversation c = this.conversation;
        final Presence.Status status;
        final String text = this.binding.textinput == null ? "" : this.binding.textinput.getText().toString();
        final SendButtonAction action;
        if (hasAttachments) {
            action = SendButtonAction.TEXT;
        } else {
            action = SendButtonTool.getAction(getActivity(), c, text);
        }
        if (useSendButtonToIndicateStatus && c.getAccount().getStatus() == Account.State.ONLINE) {
            if (activity != null && activity.xmppConnectionService != null && activity.xmppConnectionService.getMessageArchiveService().isCatchingUp(c)) {
                status = Presence.Status.OFFLINE;
            } else if (c.getMode() == Conversation.MODE_SINGLE) {
                status = c.getContact().getShownStatus();
            } else {
                status = c.getMucOptions().online() ? Presence.Status.ONLINE : Presence.Status.OFFLINE;
            }
        } else {
            status = Presence.Status.OFFLINE;
        }
        this.binding.textSendButton.setTag(action);
        final Activity activity = getActivity();
        if (activity != null) {
            this.binding.textSendButton.setImageResource(SendButtonTool.getSendButtonImageResource(activity, action, status));
        }
        updateSnackBar(conversation);
        updateChatMsgHint();
        updateTextFormat(canSendMeCommand());
    }

    protected void updateStatusMessages() {
        DateSeparator.addAll(this.messageList);
        if (showLoadMoreMessages(conversation)) {
            this.messageList.add(0, Message.createLoadMoreMessage(conversation));
        }
        if (conversation.getMode() == Conversation.MODE_MULTI) {
            final MucOptions mucOptions = conversation.getMucOptions();
            final List<MucOptions.User> allUsers = mucOptions.getUsers();
            final Set<ReadByMarker> addedMarkers = new HashSet<>();
            if (mucOptions.isPrivateAndNonAnonymous()) {
                for (int i = this.messageList.size() - 1; i >= 0; --i) {
                    final Set<ReadByMarker> markersForMessage = messageList.get(i).getReadByMarkers();
                    final List<MucOptions.User> shownMarkers = new ArrayList<>();
                    for (ReadByMarker marker : markersForMessage) {
                        if (!ReadByMarker.contains(marker, addedMarkers)) {
                            addedMarkers.add(marker); //may be put outside this condition. set should do dedup anyway
                            MucOptions.User user = mucOptions.findUser(marker);
                            if (user != null) {
                                shownMarkers.add(user);
                            }
                        }
                    }
                    final ReadByMarker markerForSender = ReadByMarker.from(messageList.get(i));
                    final Message statusMessage;
                    final int size = shownMarkers.size();
                    if (size > 1) {
                        final String body;
                        if (size <= 4) {
                            body = getString(R.string.contacts_have_read_up_to_this_point, UIHelper.concatNames(shownMarkers));
                        } else if (ReadByMarker.allUsersRepresented(allUsers, markersForMessage, markerForSender)) {
                            body = getString(R.string.everyone_has_read_up_to_this_point);
                        } else {
                            body = getString(R.string.contacts_and_n_more_have_read_up_to_this_point, UIHelper.concatNames(shownMarkers, 3), size - 3);
                        }
                        statusMessage = Message.createStatusMessage(conversation, body);
                        statusMessage.setCounterparts(shownMarkers);
                    } else if (size == 1) {
                        statusMessage = Message.createStatusMessage(conversation, getString(R.string.contact_has_read_up_to_this_point, UIHelper.getDisplayName(shownMarkers.get(0))));
                        statusMessage.setCounterpart(shownMarkers.get(0).getFullJid());
                        statusMessage.setTrueCounterpart(shownMarkers.get(0).getRealJid());
                    } else {
                        statusMessage = null;
                    }
                    if (statusMessage != null) {
                        this.messageList.add(i + 1, statusMessage);
                    }
                    addedMarkers.add(markerForSender);
                    if (ReadByMarker.allUsersRepresented(allUsers, addedMarkers)) {
                        break;
                    }
                }
            }
        }
    }


    private void stopScrolling() {
        long now = SystemClock.uptimeMillis();
        MotionEvent cancel = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0, 0, 0);
        binding.messagesView.dispatchTouchEvent(cancel);
    }

    private boolean showLoadMoreMessages(final Conversation c) {
        if (activity == null || activity.xmppConnectionService == null) {
            return false;
        }
        final boolean mam = hasMamSupport(c) && !c.getContact().isBlocked();
        final MessageArchiveService service = activity.xmppConnectionService.getMessageArchiveService();
        return mam && (c.getLastClearHistory().getTimestamp() != 0 || (c.countMessages() == 0 && c.messagesLoaded.get() && c.hasMessagesLeftOnServer() && !service.queryInProgress(c)));
    }

    private boolean hasMamSupport(final Conversation c) {
        if (c.getMode() == Conversation.MODE_SINGLE) {
            final XmppConnection connection = c.getAccount().getXmppConnection();
            return connection != null && connection.getFeatures().mam();
        } else {
            return c.getMucOptions().mamSupport();
        }
    }

    protected void showSnackbar(final String message, final int action, final OnClickListener clickListener) {
        this.binding.snackbar.setVisibility(View.VISIBLE);
        this.binding.snackbar.setOnClickListener(null);
        this.binding.snackbarMessage.setText(message);
        this.binding.snackbarMessage.setOnClickListener(null);
        this.binding.snackbarAction.setVisibility(clickListener == null ? View.GONE : View.VISIBLE);
        if (action != 0) {
            this.binding.snackbarAction.setText(action);
        }
        this.binding.snackbarAction.setOnClickListener(clickListener);
    }

    protected void showSnackbar(final int message, final int action, final OnClickListener clickListener) {
        showSnackbar(message, action, clickListener, null);
    }

    protected void showSnackbar(final int message, final int action, final OnClickListener clickListener, final View.OnLongClickListener longClickListener) {
        this.binding.snackbar.setVisibility(View.VISIBLE);
        this.binding.snackbar.setOnClickListener(null);
        this.binding.snackbarMessage.setText(message);
        this.binding.snackbarMessage.setOnClickListener(null);
        this.binding.snackbarAction.setVisibility(clickListener == null ? View.GONE : View.VISIBLE);
        if (action != 0) {
            this.binding.snackbarAction.setText(action);
        }
        this.binding.snackbarAction.setOnClickListener(clickListener);
        this.binding.snackbarAction.setOnLongClickListener(longClickListener);
    }

    protected void hideSnackbar() {
        this.binding.snackbar.setVisibility(View.GONE);
    }

    protected void sendMessage(Message message) {
        activity.xmppConnectionService.sendMessage(message);
        messageSent();
    }

    protected void sendPgpMessage(final Message message) {
        final XmppConnectionService xmppService = activity.xmppConnectionService;
        final Contact contact = message.getConversation().getContact();
        if (!activity.hasPgp()) {
            activity.showInstallPgpDialog();
            return;
        }
        if (conversation.getAccount().getPgpSignature() == null) {
            activity.announcePgp(conversation.getAccount(), conversation, null, activity.onOpenPGPKeyPublished);
            return;
        }
        if (!mSendingPgpMessage.compareAndSet(false, true)) {
            Log.d(Config.LOGTAG, "sending pgp message already in progress");
        }
        if (conversation.getMode() == Conversation.MODE_SINGLE) {
            if (contact.getPgpKeyId() != 0) {
                xmppService.getPgpEngine().hasKey(contact,
                        new UiCallback<Contact>() {

                            @Override
                            public void userInputRequired(PendingIntent pi, Contact contact) {
                                startPendingIntent(pi, REQUEST_ENCRYPT_MESSAGE);
                            }

                            @Override
                            public void progress(int progress) {

                            }

                            @Override
                            public void success(Contact contact) {
                                encryptTextMessage(message);
                            }

                            @Override
                            public void error(int error, Contact contact) {
                                activity.runOnUiThread(() -> ToastCompat.makeText(activity,
                                        R.string.unable_to_connect_to_keychain,
                                        ToastCompat.LENGTH_SHORT
                                ).show());
                                mSendingPgpMessage.set(false);
                            }
                        });

            } else {
                showNoPGPKeyDialog(false,
                        (dialog, which) -> {
                            conversation.setNextEncryption(Message.ENCRYPTION_NONE);
                            xmppService.updateConversation(conversation);
                            message.setEncryption(Message.ENCRYPTION_NONE);
                            xmppService.sendMessage(message);
                            messageSent();
                        });
            }
        } else {
            if (conversation.getMucOptions().pgpKeysInUse()) {
                if (!conversation.getMucOptions().everybodyHasKeys()) {
                    Toast warning = ToastCompat
                            .makeText(getActivity(),
                                    R.string.missing_public_keys,
                                    ToastCompat.LENGTH_LONG);
                    warning.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    warning.show();
                }
                encryptTextMessage(message);
            } else {
                showNoPGPKeyDialog(true,
                        (dialog, which) -> {
                            conversation.setNextEncryption(Message.ENCRYPTION_NONE);
                            message.setEncryption(Message.ENCRYPTION_NONE);
                            xmppService.updateConversation(conversation);
                            xmppService.sendMessage(message);
                            messageSent();
                        });
            }
        }
    }

    public void encryptTextMessage(Message message) {
        activity.xmppConnectionService.getPgpEngine().encrypt(message,
                new UiCallback<Message>() {

                    @Override
                    public void userInputRequired(PendingIntent pi, Message message) {
                        startPendingIntent(pi, REQUEST_SEND_MESSAGE);
                    }

                    @Override
                    public void progress(int progress) {

                    }

                    @Override
                    public void success(Message message) {
                        //TODO the following two call can be made before the callback
                        getActivity().runOnUiThread(() -> messageSent());
                    }

                    @Override
                    public void error(final int error, Message message) {
                        getActivity().runOnUiThread(() -> {
                            doneSendingPgpMessage();
                            ToastCompat.makeText(getActivity(), error == 0 ? R.string.unable_to_connect_to_keychain : error, ToastCompat.LENGTH_SHORT).show();
                        });

                    }
                });
    }

    public void showNoPGPKeyDialog(boolean plural, DialogInterface.OnClickListener listener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIconAttribute(android.R.attr.alertDialogIcon);
        if (plural) {
            builder.setTitle(getString(R.string.no_pgp_keys));
            builder.setMessage(getText(R.string.contacts_have_no_pgp_keys));
        } else {
            builder.setTitle(getString(R.string.no_pgp_key));
            builder.setMessage(getText(R.string.contact_has_no_pgp_key));
        }
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.setPositiveButton(getString(R.string.send_unencrypted), listener);
        builder.create().show();
    }
    protected void sendOtrMessage(final Message message) {
        final ConversationsActivity activity = (ConversationsActivity) getActivity();
        final XmppConnectionService xmppService = activity.xmppConnectionService;
        activity.selectPresence(conversation,
                () -> {
                    message.setCounterpart(conversation.getNextCounterpart());
                    xmppService.sendMessage(message);
                    messageSent();
                });
    }
    public void appendText(String text, final boolean doNotAppend) {
        if (text == null) {
            return;
        }
        final Editable editable = this.binding.textinput.getText();
        String previous = editable == null ? "" : editable.toString();
        if (doNotAppend && !TextUtils.isEmpty(previous)) {
            ToastCompat.makeText(getActivity(), R.string.already_drafting_message, ToastCompat.LENGTH_LONG).show();
            return;
        }
        if (UIHelper.isLastLineQuote(previous)) {
            text = '\n' + text;
        } else if (previous.length() != 0 && !Character.isWhitespace(previous.charAt(previous.length() - 1))) {
            text = " " + text;
        }
        this.binding.textinput.append(text);
    }

    @Override
    public boolean onEnterPressed(final boolean isCtrlPressed) {
        if (isCtrlPressed || enterIsSend()) {
            sendMessage();
            return true;
        }
        return false;
    }

    private boolean enterIsSend() {
        final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return p.getBoolean("enter_is_send", getResources().getBoolean(R.bool.enter_is_send));
    }

    public boolean onArrowUpCtrlPressed() {
        final Message lastEditableMessage = conversation == null ? null : conversation.getLastEditableMessage();
        if (lastEditableMessage != null) {
            correctMessage(lastEditableMessage);
            return true;
        } else {
            ToastCompat.makeText(getActivity(), R.string.could_not_correct_message, ToastCompat.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    public void onTypingStarted() {
        final XmppConnectionService service = activity == null ? null : activity.xmppConnectionService;
        if (service == null) {
            return;
        }
        final Account.State status = conversation.getAccount().getStatus();
        if (status == Account.State.ONLINE && conversation.setOutgoingChatState(ChatState.COMPOSING)) {
            service.sendChatState(conversation);
        }
        runOnUiThread(this::updateSendButton);

    }

    @Override
    public void onTypingStopped() {
        final XmppConnectionService service = activity == null ? null : activity.xmppConnectionService;
        if (service == null) {
            return;
        }
        final Account.State status = conversation.getAccount().getStatus();
        if (status == Account.State.ONLINE && conversation.setOutgoingChatState(ChatState.PAUSED)) {
            service.sendChatState(conversation);
        }
    }

    @Override
    public void onTextDeleted() {
        final XmppConnectionService service = activity == null ? null : activity.xmppConnectionService;
        if (service == null) {
            return;
        }
        final Account.State status = conversation.getAccount().getStatus();
        if (status == Account.State.ONLINE && conversation.setOutgoingChatState(Config.DEFAULT_CHAT_STATE)) {
            service.sendChatState(conversation);
        }
        runOnUiThread(() -> {
            if (activity == null) {
                return;
            }
            activity.onConversationsListItemUpdated();
        });
        runOnUiThread(this::updateSendButton);
    }

    @Override
    public void onTextChanged() {
        if (conversation != null && conversation.getCorrectingMessage() != null) {
            runOnUiThread(this::updateSendButton);
        }
    }

    @Override
    public boolean onTabPressed(boolean repeated) {
        if (conversation == null || conversation.getMode() == Conversation.MODE_SINGLE) {
            return false;
        }
        if (repeated) {
            completionIndex++;
        } else {
            lastCompletionLength = 0;
            completionIndex = 0;
            final String content = this.binding.textinput.getText().toString();
            lastCompletionCursor = this.binding.textinput.getSelectionEnd();
            int start = lastCompletionCursor > 0 ? content.lastIndexOf(" ", lastCompletionCursor - 1) + 1 : 0;
            firstWord = start == 0;
            incomplete = content.substring(start, lastCompletionCursor);
        }
        List<String> completions = new ArrayList<>();
        for (MucOptions.User user : conversation.getMucOptions().getUsers()) {
            String name = user.getName();
            if (name != null && name.startsWith(incomplete)) {
                completions.add(name + (firstWord ? ": " : " "));
            }
        }
        Collections.sort(completions);
        if (completions.size() > completionIndex) {
            String completion = completions.get(completionIndex).substring(incomplete.length());
            this.binding.textinput.getEditableText().delete(lastCompletionCursor, lastCompletionCursor + lastCompletionLength);
            this.binding.textinput.getEditableText().insert(lastCompletionCursor, completion);
            lastCompletionLength = completion.length();
        } else {
            completionIndex = -1;
            this.binding.textinput.getEditableText().delete(lastCompletionCursor, lastCompletionCursor + lastCompletionLength);
            lastCompletionLength = 0;
        }
        return true;
    }

    private boolean messageContainsQuery(Message m, String q) {
        return m != null && m.getMergedBody().toString().toLowerCase().contains(q.toLowerCase());
    }

    private void startPendingIntent(PendingIntent pendingIntent, int requestCode) {
        try {
            getActivity().startIntentSenderForResult(pendingIntent.getIntentSender(), requestCode, null, 0, 0, 0);
        } catch (final SendIntentException ignored) {
        }
    }

    @Override
    public void onBackendConnected() {
        Log.d(Config.LOGTAG, "ConversationFragment.onBackendConnected()");
        String uuid = pendingConversationsUuid.pop();
        if (uuid != null) {
            if (!findAndReInitByUuidOrArchive(uuid)) {
                return;
            }
        } else {
            if (!activity.xmppConnectionService.isConversationStillOpen(conversation)) {
                clearPending();
                activity.onConversationArchived(conversation);
                return;
            }
        }
        ActivityResult activityResult = postponedActivityResult.pop();
        if (activityResult != null) {
            handleActivityResult(activityResult);
        }
        clearPending();
    }

    private boolean findAndReInitByUuidOrArchive(@NonNull final String uuid) {
        Conversation conversation = activity.xmppConnectionService.findConversationByUuid(uuid);
        if (conversation == null) {
            clearPending();
            activity.onConversationArchived(null);
            return false;
        }
        reInit(conversation);
        ScrollState scrollState = pendingScrollState.pop();
        String lastMessageUuid = pendingLastMessageUuid.pop();
        List<Attachment> attachments = pendingMediaPreviews.pop();
        if (scrollState != null) {
            setScrollPosition(scrollState, lastMessageUuid);
        }
        if (attachments != null && attachments.size() > 0) {
            Log.d(Config.LOGTAG, "had attachments on restore");
            mediaPreviewAdapter.addMediaPreviews(attachments);
            toggleInputMethod();
        }
        return true;
    }

    private void clearPending() {
        if (postponedActivityResult.clear()) {
            Log.e(Config.LOGTAG, "cleared pending intent with unhandled result left");
            if (pendingTakePhotoUri.clear()) {
                Log.e(Config.LOGTAG, "cleared pending photo uri");
            }
        }
        if (pendingScrollState.clear()) {
            Log.e(Config.LOGTAG, "cleared scroll state");
        }
        if (pendingConversationsUuid.clear()) {
            Log.e(Config.LOGTAG, "cleared pending conversations uuid");
        }
        if (pendingMediaPreviews.clear()) {
            Log.e(Config.LOGTAG, "cleared pending media previews");
        }
    }

    public Conversation getConversation() {
        return conversation;
    }

    @Override
    public void onContactPictureLongClicked(View v, final Message message) {
        final String fingerprint;
        if (message.getEncryption() == Message.ENCRYPTION_PGP || message.getEncryption() == Message.ENCRYPTION_DECRYPTED) {
            fingerprint = "pgp";
        } else {
            fingerprint = message.getFingerprint();
        }
        final PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        final Contact contact = message.getContact();
        if (message.getStatus() <= Message.STATUS_RECEIVED && (contact == null || !contact.isSelf())) {
            if (message.getConversation().getMode() == Conversation.MODE_MULTI) {
                final Jid cp = message.getCounterpart();
                if (cp == null || cp.isBareJid()) {
                    return;
                }
                final Jid tcp = message.getTrueCounterpart();
                final User userByRealJid = tcp != null ? conversation.getMucOptions().findOrCreateUserByRealJid(tcp, cp) : null;
                final User user = userByRealJid != null ? userByRealJid : conversation.getMucOptions().findUserByFullJid(cp);
                popupMenu.inflate(R.menu.muc_details_context);
                final Menu menu = popupMenu.getMenu();
                MucDetailsContextMenuHelper.configureMucDetailsContextMenu(activity, menu, conversation, user, true, getUsername(message));
                popupMenu.setOnMenuItemClickListener(menuItem -> MucDetailsContextMenuHelper.onContextItemSelected(menuItem, user, activity, fingerprint));
            } else {
                popupMenu.inflate(R.menu.one_on_one_context);
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.action_show_avatar:
                            activity.ShowAvatarPopup(activity, contact);
                            break;
                        case R.id.action_contact_details:
                            activity.switchToContactDetails(message.getContact(), fingerprint);
                            break;
                        case R.id.action_show_qr_code:
                            activity.showQrCode("xmpp:" + message.getContact().getJid().asBareJid().toEscapedString());
                            break;
                    }
                    return true;
                });
            }
        } else {
            popupMenu.inflate(R.menu.account_context);
            final Menu menu = popupMenu.getMenu();
            popupMenu.setOnMenuItemClickListener(item -> {
                final XmppActivity activity = this.activity;
                if (activity == null) {
                    Log.e(Config.LOGTAG, "Unable to perform action. no context provided");
                    return true;
                }
                switch (item.getItemId()) {


                    case R.id.action_show_qr_code:
                        activity.showQrCode(conversation.getAccount().getShareableUri());
                        break;
                    case R.id.action_account_details:
                        activity.switchToAccount(message.getConversation().getAccount(), fingerprint);
                        break;
                }
                return true;
            });
        }
        popupMenu.show();
    }

    public String getUsername(Message message) {
        if (message == null) {
            return null;
        }
        String user;
        try {
            final Contact contact = message.getContact();
            if (conversation.getMode() == Conversation.MODE_MULTI) {
                if (contact != null) {
                    user = contact.getDisplayName();
                } else {
                    user = UIHelper.getDisplayedMucCounterpart(message.getCounterpart());
                }
            } else {
                user = contact != null ? contact.getDisplayName() : null;
            }
            if (message.getStatus() == Message.STATUS_SEND
                    || message.getStatus() == Message.STATUS_SEND_FAILED
                    || message.getStatus() == Message.STATUS_SEND_RECEIVED
                    || message.getStatus() == Message.STATUS_SEND_DISPLAYED) {
                user = getString(R.string.me);
            }
        } catch (Exception e) {
            e.printStackTrace();
            user = null;
        }
        return user;
    }

    @Override
    public void onContactPictureClicked(Message message) {
        String fingerprint;
        if (message.getEncryption() == Message.ENCRYPTION_PGP || message.getEncryption() == Message.ENCRYPTION_DECRYPTED) {
            fingerprint = "pgp";
        } else {
            fingerprint = message.getFingerprint();
        }
        final boolean received = message.getStatus() <= Message.STATUS_RECEIVED;
        if (received) {
            if (message.getConversation() instanceof Conversation && message.getConversation().getMode() == Conversation.MODE_MULTI) {
                Jid tcp = message.getTrueCounterpart();
                Jid user = message.getCounterpart();
                if (user != null && !user.isBareJid()) {
                    final MucOptions mucOptions = ((Conversation) message.getConversation()).getMucOptions();
                    if (mucOptions.participating() || ((Conversation) message.getConversation()).getNextCounterpart() != null) {
                        if (!mucOptions.isUserInRoom(user) && mucOptions.findUserByRealJid(tcp == null ? null : tcp.asBareJid()) == null) {
                            ToastCompat.makeText(getActivity(), activity.getString(R.string.user_has_left_conference, user.getResource()), ToastCompat.LENGTH_SHORT).show();
                        }
                        highlightInConference(user.getResource());
                    } else {
                        ToastCompat.makeText(getActivity(), R.string.you_are_not_participating, ToastCompat.LENGTH_SHORT).show();
                    }
                }
                return;
            } else {
                if (!message.getContact().isSelf()) {
                    activity.switchToContactDetails(message.getContact(), fingerprint);
                    return;
                }
            }
        }
        activity.switchToAccount(message.getConversation().getAccount(), fingerprint);
    }

    private Activity requireActivity() {
        final Activity activity = getActivity();
        if (activity == null) {
            throw new IllegalStateException("Activity not attached");
        }
        return activity;
    }

    private void showTextFormat(final boolean me) {
        this.binding.textformat.setVisibility(View.VISIBLE);
        this.binding.me.setEnabled(me);
        this.binding.me.setOnClickListener(meCommand);
        this.binding.quote.setOnClickListener(quote);
        this.binding.bold.setOnClickListener(boldText);
        this.binding.italic.setOnClickListener(italicText);
        this.binding.monospace.setOnClickListener(monospaceText);
        this.binding.strikethrough.setOnClickListener(strikethroughText);
        this.binding.help.setOnClickListener(help);
        this.binding.close.setOnClickListener(close);
        if (Compatibility.runsTwentyEight()) {
            this.binding.me.setTooltipText(activity.getString(R.string.me));
            this.binding.quote.setTooltipText(activity.getString(R.string.quote));
            this.binding.bold.setTooltipText(activity.getString(R.string.bold));
            this.binding.italic.setTooltipText(activity.getString(R.string.italic));
            this.binding.monospace.setTooltipText(activity.getString(R.string.monospace));
            this.binding.monospace.setTooltipText(activity.getString(R.string.monospace));
            this.binding.strikethrough.setTooltipText(activity.getString(R.string.strikethrough));
            this.binding.help.setTooltipText(activity.getString(R.string.help));
            this.binding.close.setTooltipText(activity.getString(R.string.close));
        }
    }

    private void hideTextFormat() {
        this.binding.textformat.setVisibility(View.GONE);
    }
}
