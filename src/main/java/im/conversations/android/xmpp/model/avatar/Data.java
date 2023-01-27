package im.conversations.android.xmpp.model.avatar;

import eu.siacs.conversations.xml.Namespace;
import im.conversations.android.annotation.XmlElement;
import im.conversations.android.xmpp.model.Extension;

@XmlElement(namespace = Namespace.AVATAR_DATA)
public class Data extends Extension {

    public Data() {
        super(Data.class);
    }
}
