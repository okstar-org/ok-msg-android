package im.conversations.android.xmpp.model.avatar;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import eu.siacs.conversations.xml.Namespace;
import im.conversations.android.annotation.XmlElement;
import im.conversations.android.xmpp.model.Extension;

@XmlElement(namespace = Namespace.AVATAR_DATA)
public class Data extends Extension {

    public Data() {
        super(Data.class);
    }

    public byte[] asBytes() {
        final var content = this.getContent();
        if (Strings.isNullOrEmpty(content)) {
            throw new IllegalStateException("Avatar data element is lacking content");
        }
        final var contentCleaned = CharMatcher.whitespace().removeFrom(content);
        if (BaseEncoding.base64().canDecode(contentCleaned)) {
            return BaseEncoding.base64().decode(contentCleaned);
        } else {
            throw new IllegalStateException("Avatar data element contains invalid base64");
        }
    }
}
