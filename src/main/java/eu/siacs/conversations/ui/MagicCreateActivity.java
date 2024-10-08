package eu.siacs.conversations.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import eu.siacs.conversations.BuildConfig;
import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;
import eu.siacs.conversations.cloud.FederalInfo;
import eu.siacs.conversations.cloud.OkCloudBackend;
import eu.siacs.conversations.databinding.ActivityMagicCreateBinding;
import eu.siacs.conversations.entities.SignUpForm;
import eu.siacs.conversations.entities.SignUpResult;
import eu.siacs.conversations.http.HttpConnectionManager;
import eu.siacs.conversations.stack.OkStackBackend;
import eu.siacs.conversations.stack.Res;
import eu.siacs.conversations.utils.InstallReferrerUtils;
import eu.siacs.conversations.utils.StringUtils;
import eu.siacs.conversations.xmpp.Jid;
import me.drakeet.support.toast.ToastCompat;
import okhttp3.HttpUrl;

public class MagicCreateActivity extends XmppActivity
        implements TextWatcher, AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {


    private boolean useOwnProvider = false;
    private boolean registerFromUri = false;
    public static final String EXTRA_DOMAIN = "domain";
    public static final String EXTRA_PRE_AUTH = "pre_auth";
    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_REGISTER = "register";

    private ActivityMagicCreateBinding binding;
    private String domain;
    private String username;
    private String preAuth;

    final ExecutorService executorService = Executors.newCachedThreadPool();
    /**
     * 服务提供商
     */
    private List<FederalInfo.State> states;
    private FederalInfo.State selectedState;

    private void setupHyperlink() {
        TextView linkTextView = findViewById(R.id.activity_main_link);
        TextView link2TextView = findViewById(R.id.instructions);
        linkTextView.setMovementMethod(LinkMovementMethod.getInstance());
        link2TextView.setMovementMethod(LinkMovementMethod.getInstance());

    }


    @Override
    protected void refreshUiReal() {

    }

    @Override
    protected void onBackendConnected() {

    }

    @Override
    public void onStart() {
        super.onStart();
        final int theme = findTheme();
        if (this.mTheme != theme) {
            recreate();
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        final Intent data = getIntent();
        if (data != null) {
            this.domain = data.getStringExtra(EXTRA_DOMAIN);
            this.preAuth = data.getStringExtra(EXTRA_PRE_AUTH);
            this.username = data.getStringExtra(EXTRA_USERNAME);
            this.registerFromUri = data.getBooleanExtra(EXTRA_REGISTER, false);
        } else {
            this.domain = null;
            this.preAuth = null;
            this.username = null;
            this.registerFromUri = false;
        }
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        super.onCreate(savedInstanceState);
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_magic_create);

        /**
         * 获取服务提供商
         */
        Handler handler1 = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                FederalInfo federalInfo = (FederalInfo) msg.obj;
                if (federalInfo != null) {

                    states = federalInfo.getStates().stream()
                            .filter(e -> !StringUtils.isEmpty(e.getXmppHost()))
                            .collect(Collectors.toList());
                    Log.i(Config.LOGTAG, "states:"+ states);

                    final ArrayAdapter<String> adapter = new ArrayAdapter<>(MagicCreateActivity.this,
                            android.R.layout.simple_selectable_list_item,  states.stream().map(e -> e.getName()).collect(Collectors.toList()));
                    int defaultServer = adapter.getPosition(Config.MAGIC_CREATE_DOMAIN);
                    if (registerFromUri && !useOwnProvider && (MagicCreateActivity.this.preAuth != null || domain != null)) {
                        binding.server.setEnabled(false);
                        binding.server.setVisibility(View.GONE);
                        binding.useOwn.setEnabled(false);
                        binding.useOwn.setChecked(true);
                        binding.useOwn.setVisibility(View.GONE);
                        binding.servertitle.setText(R.string.your_server);
                        binding.yourserver.setVisibility(View.VISIBLE);
                        binding.yourserver.setText(domain);
                    } else {
                        binding.yourserver.setVisibility(View.GONE);
                    }
                    binding.useOwn.setOnCheckedChangeListener(MagicCreateActivity.this);
                    binding.server.setAdapter(adapter);
                    binding.server.setSelection(defaultServer);
                    binding.server.setOnItemSelectedListener(MagicCreateActivity.this);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                }
            }
        };

        Runnable r1 = () -> {
            FederalInfo obj = OkCloudBackend.GetFederalInfo();
            if (obj == null) {
                return;
            }

            Message message = new Message();
            message.obj = obj;
            handler1.sendMessage(message);
        };
        executorService.execute(r1);

        setSupportActionBar((Toolbar) this.binding.toolbar);
        configureActionBar(getSupportActionBar(), this.domain == null);
//        if (username != null && domain != null) {
//            binding.title.setText(R.string.your_server_invitation);
//            binding.instructions.setText(getString(R.string.magic_create_text_fixed, domain));
//            binding.username.setEnabled(false);
//            binding.username.setText(this.username);
//            updateFullJidInformation(this.username);
//        } else if (domain != null) {
//            binding.instructions.setText(getString(R.string.magic_create_text_on_x, domain));
//        }
        binding.createAccount.setOnClickListener(v -> {
            try {
                final String email = binding.email.getText().toString();
                if (StringUtils.isEmpty(email)) {
                    binding.email.setError("请输入有效邮箱地址！");
                    return;
                }

                final String password = binding.password.getText().toString();
                if (StringUtils.isEmpty(password)) {
                    binding.password.setError("请输入有效密码！");
                    return;
                }
                final String confirmPassword = binding.confirmPassword.getText().toString();
                if (!Objects.equals(password, confirmPassword)) {
                    binding.confirmPassword.setError("请确认两次密码是否一致！");
                    return;
                }

                Log.i(Config.LOGTAG, "email:" + email);

                //设置进度显示
                binding.progressBar.setVisibility(View.VISIBLE);

                Handler handler = new Handler(Objects.requireNonNull(Looper.myLooper())) {
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        Res<SignUpResult> res = (Res<SignUpResult>) msg.obj;
                        if (res == null) {
                            ToastCompat.makeText(MagicCreateActivity.this, "服务器连接错误，请稍后再试...", ToastCompat.LENGTH_SHORT).show();
                            return;
                        }

                        if (!res.success()) {
                            ToastCompat.makeText(MagicCreateActivity.this, res.getMsg(), ToastCompat.LENGTH_SHORT).show();
                            return;
                        }

                        ToastCompat.makeText(MagicCreateActivity.this, "注册成功，正在进入登录界面", ToastCompat.LENGTH_SHORT).show();

                        SignUpResult result = res.getData();
                        Intent intent = new Intent(MagicCreateActivity.this, EditAccountActivity.class);
                        intent.putExtra("username", result.getUsername());
                        intent.putExtra("password", password);
                        intent.putExtra("email", email);
                        intent.putExtra("init", true);
                        intent.putExtra("existing", false);
                        intent.putExtra("useownprovider", true);
                        intent.putExtra("register", registerFromUri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        StartConversationActivity.addInviteUri(intent, getIntent());
                        startActivity(intent);
                        overridePendingTransition(R.animator.fade_in, R.animator.fade_out);
                        finish();
                        overridePendingTransition(R.animator.fade_in, R.animator.fade_out);
                    }
                };


                Runnable r = () -> {
                    Message msg = Message.obtain();
                    msg.obj = doSignUp(email, password);
                    handler.sendMessage(msg);

                    binding.progressBar.setVisibility(View.INVISIBLE);
                };

                executorService.execute(r);

//                final boolean fixedUsername;
//                final Jid jid;
//                if (this.domain != null && this.username != null) {
//                    fixedUsername = true;
//                    jid = Jid.ofLocalAndDomainEscaped(this.username, this.domain);
//                } else if (this.domain != null) {
//                    fixedUsername = false;
//                    jid = Jid.ofLocalAndDomainEscaped(username, this.domain);
//                } else {
//                    fixedUsername = false;
//                    domain = updateDomain();
//                    jid = Jid.ofLocalAndDomainEscaped(username, domain);
//                }
//                if (!jid.getEscapedLocal().equals(jid.getLocal())) {
//                    binding.username.setError(getString(R.string.invalid_username));
//                    binding.username.requestFocus();
//                } else {
//                    binding.username.setError(null);
//                    Account account = xmppConnectionService.findAccountByJid(jid);
//                    String password = CryptoHelper.createPassword(new SecureRandom());
//                    if (account == null) {
//                        account = new Account(jid, password);
//                        account.setOption(Account.OPTION_REGISTER, true);
//                        account.setOption(Account.OPTION_DISABLED, true);
//                        account.setOption(Account.OPTION_MAGIC_CREATE, true);
//                        account.setOption(Account.OPTION_FIXED_USERNAME, fixedUsername);
//                        if (this.preAuth != null) {
//                            account.setKey(Account.KEY_PRE_AUTH_REGISTRATION_TOKEN, this.preAuth);
//                        }
//                        xmppConnectionService.createAccount(account);
//                    }
//                    Intent intent = new Intent(MagicCreateActivity.this, EditAccountActivity.class);
//                    intent.putExtra("jid", account.getJid().asBareJid().toString());
//                    intent.putExtra("init", true);
//                    intent.putExtra("existing", false);
//                    intent.putExtra("useownprovider", useOwnProvider);
//                    intent.putExtra("register", registerFromUri);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                    builder.setTitle(getString(R.string.create_account));
//                    builder.setCancelable(false);
//                    StringBuilder messasge = new StringBuilder();
//                    messasge.append(getString(R.string.secure_password_generated));
//                    messasge.append("\n\n");
//                    messasge.append(getString(R.string.password));
//                    messasge.append(": ");
//                    messasge.append(password);
//                    messasge.append("\n\n");
//                    messasge.append(getString(R.string.change_password_in_next_step));
//                    builder.setMessage(messasge);
//                    builder.setPositiveButton(getString(R.string.copy_to_clipboard), (dialogInterface, i) -> {
//                        if (copyTextToClipboard(password, R.string.create_account)) {
//                            StartConversationActivity.addInviteUri(intent, getIntent());
//                            startActivity(intent);
//                            overridePendingTransition(R.animator.fade_in, R.animator.fade_out);
//                            finish();
//                            overridePendingTransition(R.animator.fade_in, R.animator.fade_out);
//                        }
//                    });
//                    builder.create().show();
//                }
            } catch (IllegalArgumentException e) {
//                binding.username.setError(getString(R.string.invalid_username));
//                binding.username.requestFocus();

            }
        });
//        binding.username.addTextChangedListener(this);
//        setupHyperlink();
    }

    private Res<SignUpResult> doSignUp(String email, String password) {
        if (this.selectedState == null) {
            Log.w(Config.LOGTAG, "Not select state.");
            return null;
        }
       return OkStackBackend.Get(selectedState).signUp(email, password);
    }

    private String updateDomain() {
        String getUpdatedDomain = null;
        if (domain == null && !useOwnProvider) {
            getUpdatedDomain = Config.MAGIC_CREATE_DOMAIN;
        }
        if (useOwnProvider) {
            getUpdatedDomain = "your-domain.com";
        }
        return getUpdatedDomain;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        updateFullJidInformation(s.toString());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//        updateFullJidInformation(binding.username.getText().toString());
        if(parent.getId() == R.id.server){
            //选择的服务商
            Object item = parent.getItemAtPosition(position);
            Log.i(Config.LOGTAG, "provider: " + position + "=>" + item);

            this.selectedState = states.get(position);
            Log.i(Config.LOGTAG, "Select state: "+this.selectedState);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(SettingsActivity.SELECTED_FEDERAL_STATE_STACK_URL, this.selectedState.getStackUrl());
            editor.apply();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
//        updateFullJidInformation(binding.username.getText().toString());
    }

    private void updateFullJidInformation(String username) {
        if (useOwnProvider && !registerFromUri) {
            this.domain = updateDomain();
        } else if (!registerFromUri) {
            this.domain = binding.server.getSelectedItem().toString();
        }
        if (username.trim().isEmpty()) {
            binding.fullJid.setVisibility(View.INVISIBLE);
        } else {
            try {
                binding.fullJid.setVisibility(View.VISIBLE);
                final Jid jid;
                if (this.domain == null) {
                    jid = Jid.ofLocalAndDomainEscaped(username, Config.MAGIC_CREATE_DOMAIN);
                } else {
                    jid = Jid.ofLocalAndDomainEscaped(username, this.domain);
                }
                binding.fullJid.setText(getString(R.string.your_full_jid_will_be, jid.toEscapedString()));
            } catch (IllegalArgumentException e) {
                binding.fullJid.setVisibility(View.INVISIBLE);
            }

        }
    }

    @Override
    public void onDestroy() {
        InstallReferrerUtils.markInstallReferrerExecuted(this);
        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (binding.useOwn.isChecked()) {
            binding.server.setEnabled(false);
            binding.fullJid.setVisibility(View.GONE);
            useOwnProvider = true;

        } else {
            binding.server.setEnabled(true);
            binding.fullJid.setVisibility(View.VISIBLE);
            useOwnProvider = false;
        }
        registerFromUri = false;
//        updateFullJidInformation(binding.username.getText().toString());
    }
}