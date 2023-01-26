package im.conversations.android.xmpp.manager;

import android.content.Context;
import im.conversations.android.xmpp.XmppConnection;
import im.conversations.android.xmpp.model.pubsub.event.Item;
import im.conversations.android.xmpp.model.pubsub.event.Retract;
import java.util.Collection;

public class BookmarkManager extends AbstractManager {
    public BookmarkManager(Context context, XmppConnection connection) {
        super(context, connection);
    }

    public void fetch() {}

    public void updateItems(Collection<Item> items) {}

    public void deleteItems(Collection<Retract> retractions) {}

    public void deleteAllItems() {}
}
