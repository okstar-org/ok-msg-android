package im.conversations.android.xmpp.model.pubsub.event;

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

    public Collection<Retract> getRetractions() {
        return this.getExtensions(Retract.class);
    }
}
