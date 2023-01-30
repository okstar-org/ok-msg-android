package im.conversations.android.xmpp.manager;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import eu.siacs.conversations.xml.Namespace;
import eu.siacs.conversations.xmpp.Jid;
import im.conversations.android.xmpp.IqErrorException;
import im.conversations.android.xmpp.XmppConnection;
import im.conversations.android.xmpp.model.axolotl.Bundle;
import im.conversations.android.xmpp.model.axolotl.DeviceList;
import im.conversations.android.xmpp.model.pubsub.Items;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeoutException;
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
        getDatabase().axolotlDao().setDeviceList(getAccount(), from, deviceIds);
    }

    public ListenableFuture<Set<Integer>> fetchDeviceIds(final Jid address) {
        final var deviceIdsFuture =
                Futures.transform(
                        getManager(PubSubManager.class)
                                .fetchMostRecentItem(
                                        address, Namespace.AXOLOTL_DEVICE_LIST, DeviceList.class),
                        DeviceList::getDeviceIds,
                        MoreExecutors.directExecutor());
        Futures.addCallback(
                deviceIdsFuture,
                new FutureCallback<>() {
                    @Override
                    public void onSuccess(Set<Integer> deviceIds) {
                        getDatabase().axolotlDao().setDeviceList(getAccount(), address, deviceIds);
                    }

                    @Override
                    public void onFailure(@NonNull Throwable throwable) {
                        if (throwable instanceof TimeoutException) {
                            return;
                        }
                        if (throwable instanceof IqErrorException) {
                            final var iqErrorException = (IqErrorException) throwable;
                            final var error = iqErrorException.getError();
                            final var condition = error == null ? null : error.getCondition();
                            if (condition != null) {
                                getDatabase()
                                        .axolotlDao()
                                        .setDeviceListError(getAccount(), address, condition);
                                return;
                            }
                        }
                        getDatabase().axolotlDao().setDeviceListParsingError(getAccount(), address);
                    }
                },
                MoreExecutors.directExecutor());
        return deviceIdsFuture;
    }

    public ListenableFuture<Bundle> fetchBundle(final Jid address, final int deviceId) {
        final var node = String.format(Locale.ROOT, "%s:%d", Namespace.AXOLOTL_BUNDLES, deviceId);
        return getManager(PubSubManager.class).fetchMostRecentItem(address, node, Bundle.class);
    }
}
