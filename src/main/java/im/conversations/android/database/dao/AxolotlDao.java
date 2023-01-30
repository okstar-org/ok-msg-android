package im.conversations.android.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Transaction;
import com.google.common.collect.Collections2;
import eu.siacs.conversations.xmpp.Jid;
import im.conversations.android.database.entity.AxolotlDeviceListEntity;
import im.conversations.android.database.entity.AxolotlDeviceListItemEntity;
import im.conversations.android.database.model.Account;
import im.conversations.android.xmpp.model.error.Condition;
import java.util.Collection;
import java.util.Set;

@Dao
public abstract class AxolotlDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract long insert(AxolotlDeviceListEntity entity);

    @Insert
    protected abstract void insert(Collection<AxolotlDeviceListItemEntity> entities);

    @Transaction
    public void setDeviceList(Account account, Jid from, Set<Integer> deviceIds) {
        final var listId = insert(AxolotlDeviceListEntity.of(account.id, from));
        insert(
                Collections2.transform(
                        deviceIds, deviceId -> AxolotlDeviceListItemEntity.of(listId, deviceId)));
    }

    @Transaction
    public void setDeviceListError(final Account account, final Jid address, Condition condition) {
        insert(AxolotlDeviceListEntity.of(account.id, address, condition.getName()));
    }

    @Transaction
    public void setDeviceListParsingError(final Account account, final Jid address) {
        insert(AxolotlDeviceListEntity.ofParsingIssue(account.id, address));
    }
}
