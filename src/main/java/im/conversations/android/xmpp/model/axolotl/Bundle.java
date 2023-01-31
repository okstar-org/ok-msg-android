package im.conversations.android.xmpp.model.axolotl;

import com.google.common.collect.Iterables;
import im.conversations.android.annotation.XmlElement;
import im.conversations.android.xmpp.model.Extension;
import java.util.Collection;
import java.util.Collections;

@XmlElement
public class Bundle extends Extension {

    public Bundle() {
        super(Bundle.class);
    }

    public SignedPreKey getSignedPreKey() {
        return this.getExtension(SignedPreKey.class);
    }

    public SignedPreKeySignature getSignedPreKeySignature() {
        return this.getExtension(SignedPreKeySignature.class);
    }

    public IdentityKey getIdentityKey() {
        return this.getExtension(IdentityKey.class);
    }

    public PreKey getRandomPreKey() {
        final var preKeys = this.getExtension(PreKeys.class);
        final Collection<PreKey> preKeyList =
                preKeys == null ? Collections.emptyList() : preKeys.getExtensions(PreKey.class);
        return Iterables.get(preKeyList, (int) (preKeyList.size() * Math.random()), null);
    }
}
