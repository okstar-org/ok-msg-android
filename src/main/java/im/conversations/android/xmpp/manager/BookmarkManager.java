package im.conversations.android.xmpp.manager;

import android.content.Context;
import com.google.common.collect.Collections2;
import eu.siacs.conversations.xmpp.Jid;
import im.conversations.android.database.entity.BookmarkEntity;
import im.conversations.android.xmpp.XmppConnection;
import im.conversations.android.xmpp.model.bookmark.Conference;
import im.conversations.android.xmpp.model.pubsub.Items;
import im.conversations.android.xmpp.model.pubsub.event.Retract;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class BookmarkManager extends AbstractManager {
    public BookmarkManager(Context context, XmppConnection connection) {
        super(context, connection);
    }

    public void fetch() {
        final var future = getManager(PubSubManager.class).fetchItems(Conference.class);
    }

    private void updateItems(final Map<String, Conference> items) {
        getDatabase().bookmarkDao().updateItems(getAccount(), items);
    }

    private void deleteItems(Collection<Retract> retractions) {
        final Collection<Jid> addresses =
                Collections2.transform(retractions, r -> BookmarkEntity.jidOrNull(r.getId()));
        getDatabase()
                .bookmarkDao()
                .delete(getAccount().id, Collections2.filter(addresses, Objects::nonNull));
    }

    public void deleteAllItems() {
        getDatabase().bookmarkDao().deleteAll(getAccount().id);
    }

    public void handleItems(final Items items) {
        final var retractions = items.getRetractions();
        final var itemMap = items.getItemMap(Conference.class);
        if (retractions.size() > 0) {
            deleteItems(retractions);
        }
        if (itemMap.size() > 0) {
            updateItems(itemMap);
        }
    }
}
