package im.conversations.android.xmpp.manager;

import android.content.Context;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import eu.siacs.conversations.xmpp.Jid;
import im.conversations.android.xmpp.XmppConnection;
import im.conversations.android.xmpp.model.avatar.Info;
import im.conversations.android.xmpp.model.avatar.Metadata;
import im.conversations.android.xmpp.model.pubsub.Items;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AvatarManager extends AbstractManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvatarManager.class);

    public AvatarManager(Context context, XmppConnection connection) {
        super(context, connection);
    }

    public void handleItems(final Jid from, final Items items) {
        final var itemsMap = items.getItemMap(Metadata.class);
        final var firstEntry = Iterables.getFirst(itemsMap.entrySet(), null);
        if (firstEntry == null) {
            return;
        }
        final var itemId = firstEntry.getKey();
        final var metadata = firstEntry.getValue();
        final var info = metadata.getExtensions(Info.class);
        final var thumbnailOptional =
                Iterables.tryFind(info, i -> Objects.equals(itemId, i.getId()));
        if (thumbnailOptional.isPresent()) {
            final var thumbnail = thumbnailOptional.get();
            if (thumbnail.getUrl() != null) {
                LOGGER.warn(
                        "Thumbnail avatar from {} is hosted on remote URL. We require it to be"
                                + " hosted on PEP",
                        from);
                return;
            }
            final var additional =
                    Collections2.filter(
                            info,
                            i -> !Objects.equals(itemId, i.getId()) && Objects.nonNull(i.getUrl()));
            getDatabase().avatarDao().set(getAccount(), from.asBareJid(), thumbnail, additional);
        } else {
            LOGGER.warn(
                    "Avatar metadata from {} is lacking thumbnail (info.id must match item id",
                    from);
        }
    }
}
