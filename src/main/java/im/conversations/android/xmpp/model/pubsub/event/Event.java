package im.conversations.android.xmpp.model.pubsub.event;

import im.conversations.android.annotation.XmlElement;
import im.conversations.android.xmpp.model.Extension;

@XmlElement
public class Event extends Extension {

    public Event() {
        super(Event.class);
    }

    public ItemsWrapper getItemsWrapper() {
        return this.getExtension(ItemsWrapper.class);
    }

    public Purge getPurge() {
        return this.getExtension(Purge.class);
    }
}
