package im.conversations.android.xmpp.model.pubsub;

import im.conversations.android.annotation.XmlElement;
import im.conversations.android.xmpp.model.Extension;
import java.util.Collection;
import java.util.Collections;

@XmlElement(name = "pubsub")
public class PubSub extends Extension {

    public PubSub() {
        super(PubSub.class);
    }

    public ItemsWrapper getItemsWrapper() {
        return this.getExtension(ItemsWrapper.class);
    }

    public String getNode() {
        final ItemsWrapper itemsWrapper = getItemsWrapper();
        return itemsWrapper == null ? null : itemsWrapper.getNode();
    }

    public Collection<Item> getItems() {
        final ItemsWrapper itemsWrapper = getItemsWrapper();
        return itemsWrapper == null ? Collections.emptyList() : itemsWrapper.getItems();
    }
}
