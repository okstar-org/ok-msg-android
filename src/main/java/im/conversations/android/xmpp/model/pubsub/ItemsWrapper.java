package im.conversations.android.xmpp.model.pubsub;

import im.conversations.android.annotation.XmlElement;
import im.conversations.android.xmpp.model.Extension;
import java.util.Collection;

@XmlElement(name = "items")
public class ItemsWrapper extends Extension {

    public ItemsWrapper() {
        super(ItemsWrapper.class);
    }

    public String getNode() {
        return this.getAttribute("node");
    }

    public Collection<Item> getItems() {
        return this.getExtensions(Item.class);
    }
}
