package im.conversations.android.database;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.MasterKeys;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import im.conversations.android.database.model.Account;
import im.conversations.android.database.model.Credential;
import im.conversations.android.xmpp.sasl.ChannelBindingMechanism;
import im.conversations.android.xmpp.sasl.HashedToken;
import im.conversations.android.xmpp.sasl.SaslMechanism;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

// TODO cache credentials?!
public class CredentialStore {

    private static final String FILENAME = "credential.store";

    private static final Gson GSON = new GsonBuilder().create();

    private static volatile CredentialStore INSTANCE;

    private final Context context;

    private CredentialStore(final Context context) {
        this.context = context.getApplicationContext();
    }

    public static CredentialStore getInstance(final Context context) {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        synchronized (CredentialStore.class) {
            if (INSTANCE != null) {
                return INSTANCE;
            }
            INSTANCE = new CredentialStore(context);
            return INSTANCE;
        }
    }

    public synchronized Credential get(final Account account) {
        return getOrEmpty(account);
    }

    public void setPassword(final Account account, final String password)
            throws GeneralSecurityException, IOException {
        setPassword(account, password, false);
    }

    public synchronized void setPassword(
            final Account account, final String password, final boolean autogeneratedPassword)
            throws GeneralSecurityException, IOException {
        final Credential credential = getOrEmpty(account);
        final Credential modifiedCredential =
                new Credential(
                        password,
                        autogeneratedPassword,
                        credential.pinnedMechanism,
                        credential.pinnedChannelBinding,
                        credential.fastMechanism,
                        credential.fastToken,
                        credential.preAuthRegistrationToken,
                        credential.privateKeyAlias);
        // TODO ignore if unchanged
        this.set(account, modifiedCredential);
    }

    public void setFastToken(
            final Account account, final HashedToken.Mechanism mechanism, final String token)
            throws GeneralSecurityException, IOException {
        final Credential credential = getOrEmpty(account);
        final Credential modifiedCredential =
                new Credential(
                        credential.password,
                        credential.autogeneratedPassword,
                        credential.pinnedMechanism,
                        credential.pinnedChannelBinding,
                        mechanism.name(),
                        token,
                        credential.preAuthRegistrationToken,
                        credential.privateKeyAlias);
        // TODO ignore if unchanged
        this.set(account, modifiedCredential);
    }

    public void resetFastToken(final Account account) throws GeneralSecurityException, IOException {
        final Credential credential = getOrEmpty(account);
        final Credential modifiedCredential =
                new Credential(
                        credential.password,
                        credential.autogeneratedPassword,
                        credential.pinnedMechanism,
                        credential.pinnedChannelBinding,
                        null,
                        null,
                        credential.preAuthRegistrationToken,
                        credential.privateKeyAlias);
        // TODO ignore if unchanged
        this.set(account, modifiedCredential);
    }

    public void setPinnedMechanism(final Account account, final SaslMechanism mechanism)
            throws GeneralSecurityException, IOException {
        final String pinnedMechanism = mechanism.getMechanism();
        final String pinnedChannelBinding;
        if (mechanism instanceof ChannelBindingMechanism) {
            pinnedChannelBinding =
                    ((ChannelBindingMechanism) mechanism).getChannelBinding().toString();
        } else {
            pinnedChannelBinding = null;
        }
        final Credential credential = getOrEmpty(account);
        final Credential modifiedCredential =
                new Credential(
                        credential.password,
                        credential.autogeneratedPassword,
                        pinnedMechanism,
                        pinnedChannelBinding,
                        credential.fastMechanism,
                        credential.fastToken,
                        credential.preAuthRegistrationToken,
                        credential.privateKeyAlias);
        // TODO ignore if unchanged
        this.set(account, modifiedCredential);
    }

    public void resetPinnedMechanism(final Account account)
            throws GeneralSecurityException, IOException {
        final Credential credential = getOrEmpty(account);
        final Credential modifiedCredential =
                new Credential(
                        credential.password,
                        credential.autogeneratedPassword,
                        null,
                        null,
                        credential.fastMechanism,
                        credential.fastToken,
                        credential.preAuthRegistrationToken,
                        credential.privateKeyAlias);
        // TODO ignore if unchanged
        this.set(account, modifiedCredential);
    }

    private Credential getOrEmpty(final Account account) {
        final Map<String, Credential> store = loadOrEmpty();
        final Credential credential = store.get(account.address.toEscapedString());
        return credential == null ? Credential.empty() : credential;
    }

    private void set(@NonNull final Account account, @NonNull final Credential credential)
            throws GeneralSecurityException, IOException {
        final HashMap<String, Credential> credentialStore = new HashMap<>(loadOrEmpty());
        credentialStore.put(account.address.toEscapedString(), credential);
        store(credentialStore);
    }

    private Map<String, Credential> loadOrEmpty() {
        final Map<String, Credential> store;
        try {
            store = load();
        } catch (final Exception e) {
            return ImmutableMap.of();
        }
        return store == null ? ImmutableMap.of() : store;
    }

    private Map<String, Credential> load() throws GeneralSecurityException, IOException {
        final EncryptedFile encryptedFile = getEncryptedFile();
        final FileInputStream inputStream = encryptedFile.openFileInput();
        final Type type = new TypeToken<Map<String, Credential>>() {}.getType();
        return GSON.fromJson(new InputStreamReader(inputStream), type);
    }

    private void store(final Map<String, Credential> store)
            throws GeneralSecurityException, IOException {
        final File file = getCredentialStoreFile();
        file.delete();
        final EncryptedFile encryptedFile = getEncryptedFile(file);
        try (final FileOutputStream outputStream = encryptedFile.openFileOutput()) {
            GSON.toJson(store, new OutputStreamWriter(outputStream));
            outputStream.flush();
        }
    }

    private EncryptedFile getEncryptedFile() throws GeneralSecurityException, IOException {
        return getEncryptedFile(getCredentialStoreFile());
    }

    private EncryptedFile getEncryptedFile(final File file)
            throws GeneralSecurityException, IOException {
        final KeyGenParameterSpec keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC;
        final String mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec);
        return new EncryptedFile.Builder(
                        file,
                        context,
                        mainKeyAlias,
                        EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB)
                .build();
    }

    private File getCredentialStoreFile() {
        return new File(context.getFilesDir(), FILENAME);
    }
}
