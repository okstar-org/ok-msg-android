package eu.siacs.conversations.store;

import android.os.Parcelable;
import android.util.Log;

import com.tencent.mmkv.MMKV;

import eu.siacs.conversations.Config;

public class StoreManager {

    private final MMKV mmkv;

    // 私有构造方法，防止外部实例化
    private StoreManager() {
         String rootDir = MMKV.initialize(Config.application);
        Log.d(Config.LOGTAG, "rootDir :"+rootDir);
        mmkv = MMKV.defaultMMKV();
    }

    private static final class InstanceHolder {
        // 创建一个私有的静态成员变量来存储单例对象
        static final StoreManager instance = new StoreManager();
    }

    // 提供一个公共的静态方法，作为获取单例对象的全局访问点
    public static StoreManager getInstance() {
        // 使用双重检查锁定 (Double-Checked Locking) 来优化性能
        return InstanceHolder.instance;
    }

    public boolean putData(String key, Object obj) {
        boolean encodeSuccess = false;
        if (key != null && obj != null) {
            if (obj instanceof Boolean) {
                encodeSuccess = mmkv.encode(key, (Boolean) obj);
            } else if (obj instanceof Integer) {
                encodeSuccess = mmkv.encode(key, (Integer) obj);
            } else if (obj instanceof Long) {
                encodeSuccess = mmkv.encode(key, (Long) obj);
            } else if (obj instanceof Float) {
                encodeSuccess = mmkv.encode(key, (Float) obj);
            } else if (obj instanceof Double) {
                encodeSuccess = mmkv.encode(key, (Double) obj);
            } else if (obj instanceof String) {
                encodeSuccess = mmkv.encode(key, (String) obj);
            } else if (obj instanceof Parcelable) {
                encodeSuccess = mmkv.encode(key, (Parcelable) obj);
            } else {
                encodeSuccess = mmkv.encode(key, obj.toString());
            }
        }
        return encodeSuccess;
    }

    public boolean getData(String key, boolean defaultValue) {
        return mmkv.decodeBool(key, defaultValue);
    }

    public int getData(String key, int defaultValue) {
        return mmkv.decodeInt(key, defaultValue);
    }

    public long getData(String key, long defaultValue) {
        return mmkv.decodeLong(key, defaultValue);
    }

    public float getData(String key, float defaultValue) {
        return mmkv.decodeFloat(key, defaultValue);
    }

    public double getData(String key, double defaultValue) {
        return mmkv.decodeDouble(key, defaultValue);
    }

    public String getData(String key, String defaultValue) {
        return mmkv.decodeString(key, defaultValue);
    }

    @SuppressWarnings("unchecked")
    public <T extends Parcelable> T getData(String key, Class<T> clazz) {
        return (T) mmkv.decodeParcelable(key, clazz);
    }


}
