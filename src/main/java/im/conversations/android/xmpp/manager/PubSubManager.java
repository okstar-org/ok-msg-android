package im.conversations.android.xmpp.manager;

import android.content.Context;
import eu.siacs.conversations.xml.Namespace;
import im.conversations.android.xmpp.XmppConnection;
import im.conversations.android.xmpp.model.pubsub.event.Event;
import im.conversations.android.xmpp.model.pubsub.event.ItemsWrapper;
import im.conversations.android.xmpp.model.pubsub.event.Purge;
import im.conversations.android.xmpp.model.stanza.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PubSubManager extends AbstractManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubSubManager.class);

    public PubSubManager(Context context, XmppConnection connection) {
        super(context, connection);
    }

    public void handleEvent(final Message message) {
        final var event = message.getExtension(Event.class);
        if (event.hasExtension(Purge.class)) {
            handlePurge(message);
        } else if (event.hasExtension(ItemsWrapper.class)) {
            handleItems(message);
        }
    }

    private void handleItems(final Message message) {
        final var from = message.getFrom();
        final var event = message.getExtension(Event.class);
        final var itemsWrapper = event.getItemsWrapper();
        final var node = itemsWrapper.getNode();
        final var items = itemsWrapper.getItems();
        final var retractions = itemsWrapper.getRetractions();
        if (connection.fromAccount(message) && Namespace.BOOKMARKS2.equals(node)) {
            final var bookmarkManager = getManager(BookmarkManager.class);
            if (retractions.size() > 0) {
                bookmarkManager.deleteItems(retractions);
            }
            if (items.size() > 0) {
                bookmarkManager.updateItems(items);
            }
        }
    }

    private void handlePurge(final Message message) {
        final var from = message.getFrom();
        final var event = message.getExtension(Event.class);
        final var purge = event.getPurge();
        final var node = purge.getNode();
        if (connection.fromAccount(message) && Namespace.BOOKMARKS2.equals(node)) {
            getManager(BookmarkManager.class).deleteAllItems();
        }
    }
}
