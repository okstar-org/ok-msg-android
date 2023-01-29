package im.conversations.android.xmpp.manager;

import android.content.Context;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import eu.siacs.conversations.xml.Namespace;
import eu.siacs.conversations.xmpp.Jid;
import im.conversations.android.xmpp.XmppConnection;
import im.conversations.android.xmpp.model.axolotl.DeviceList;
import im.conversations.android.xmpp.model.pubsub.Items;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AxolotlManager extends AbstractManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AxolotlManager.class);

    public AxolotlManager(Context context, XmppConnection connection) {
        super(context, connection);
    }

    public void handleItems(final Jid from, final Items items) {
        final var deviceList = items.getFirstItem(DeviceList.class);
        if (from == null || deviceList == null) {
            return;
        }
        final var deviceIds = deviceList.getDeviceIds();
        LOGGER.info("Received {} from {}", deviceIds, from);
        getDatabase().axolotlDao().set(getAccount(), from, deviceIds);
    }

    public ListenableFuture<Collection<Integer>> fetchDeviceIds(final Jid address) {
        return Futures.transform(
                getManager(PubSubManager.class)
                        .fetchMostRecentItem(
                                address, Namespace.AXOLOTL_DEVICE_LIST, DeviceList.class),
                DeviceList::getDeviceIds,
                MoreExecutors.directExecutor());
    }
}
