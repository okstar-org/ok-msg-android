package im.conversations.android.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import eu.siacs.conversations.xmpp.Jid;
import java.time.Instant;

@Entity(
        tableName = "axolotl_device_list",
        foreignKeys =
                @ForeignKey(
                        entity = AccountEntity.class,
                        parentColumns = {"id"},
                        childColumns = {"accountId"},
                        onDelete = ForeignKey.CASCADE),
        indices = {
            @Index(
                    value = {"accountId", "address"},
                    unique = true)
        })
public class AxolotlDeviceListEntity {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    @NonNull public Long accountId;

    @NonNull public Jid address;

    @NonNull public Instant receivedAt;

    public String errorCondition;

    public static AxolotlDeviceListEntity of(long accountId, Jid from) {
        final var entity = new AxolotlDeviceListEntity();
        entity.accountId = accountId;
        entity.address = from;
        entity.receivedAt = Instant.now();
        return entity;
    }
}
