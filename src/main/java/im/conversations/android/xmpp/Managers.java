package im.conversations.android.xmpp;

import android.content.Context;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import im.conversations.android.xmpp.manager.AbstractManager;
import im.conversations.android.xmpp.manager.AvatarManager;
import im.conversations.android.xmpp.manager.BlockingManager;
import im.conversations.android.xmpp.manager.BookmarkManager;
import im.conversations.android.xmpp.manager.CarbonsManager;
import im.conversations.android.xmpp.manager.DiscoManager;
import im.conversations.android.xmpp.manager.PresenceManager;
import im.conversations.android.xmpp.manager.PubSubManager;
import im.conversations.android.xmpp.manager.RosterManager;

public final class Managers {

    private Managers() {}

    public static ClassToInstanceMap<AbstractManager> initialize(
            final Context context, final XmppConnection connection) {
        return new ImmutableClassToInstanceMap.Builder<AbstractManager>()
                .put(AvatarManager.class, new AvatarManager(context, connection))
                .put(BlockingManager.class, new BlockingManager(context, connection))
                .put(BookmarkManager.class, new BookmarkManager(context, connection))
                .put(CarbonsManager.class, new CarbonsManager(context, connection))
                .put(DiscoManager.class, new DiscoManager(context, connection))
                .put(PresenceManager.class, new PresenceManager(context, connection))
                .put(PubSubManager.class, new PubSubManager(context, connection))
                .put(RosterManager.class, new RosterManager(context, connection))
                .build();
    }
}
