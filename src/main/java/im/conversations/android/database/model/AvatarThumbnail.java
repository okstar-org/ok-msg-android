package im.conversations.android.database.model;

import im.conversations.android.xmpp.model.avatar.Info;

public class AvatarThumbnail {

    public final String id;
    public final String type;
    public final long bytes;
    public final long height;
    public final long width;

    public AvatarThumbnail(String id, String type, long bytes, long height, long width) {
        this.id = id;
        this.type = type;
        this.bytes = bytes;
        this.height = height;
        this.width = width;
    }

    public static AvatarThumbnail of(Info info) {
        return new AvatarThumbnail(
                info.getId(), info.getType(), info.getBytes(), info.getHeight(), info.getWidth());
    }
}
