package im.conversations.android.database.model;

public class Credential {

    public final String password;
    public final boolean autogeneratedPassword;
    public final String pinnedMechanism;
    public final String pinnedChannelBinding;

    public final String fastMechanism;
    public final String fastToken;

    public final String preAuthRegistrationToken;

    public final String privateKeyAlias;

    private Credential() {
        this.password = null;
        this.autogeneratedPassword = false;
        this.pinnedMechanism = null;
        this.pinnedChannelBinding = null;
        this.fastMechanism = null;
        this.fastToken = null;
        this.preAuthRegistrationToken = null;
        this.privateKeyAlias = null;
    }

    public Credential(
            String password,
            boolean autogeneratedPassword,
            String pinnedMechanism,
            String pinnedChannelBinding,
            String fastMechanism,
            String fastToken,
            String preAuthRegistrationToken,
            String privateKeyAlias) {
        this.password = password;
        this.autogeneratedPassword = autogeneratedPassword;
        this.pinnedMechanism = pinnedMechanism;
        this.pinnedChannelBinding = pinnedChannelBinding;
        this.fastMechanism = fastMechanism;
        this.fastToken = fastToken;
        this.preAuthRegistrationToken = preAuthRegistrationToken;
        this.privateKeyAlias = privateKeyAlias;
    }

    public static Credential empty() {
        return new Credential();
    }
}