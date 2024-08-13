package org.okstar.okmsg.store.disk.cache;


import android.util.Log;

import java.io.File;

import eu.siacs.conversations.Config;

/**
 * /data/user/0/org.okstar.okmsg/cache
 */
public class CacheManager {

    // 直接初始化一个实例对象
    private static final CacheManager instance = new CacheManager();

    private File cacheDir;

    // 私有构造函数，防止外部实例化
    private CacheManager() {
        cacheDir = Config.application.getCacheDir();
        Log.d(Config.LOGTAG,"cacheDir :"+cacheDir.getAbsolutePath());
    }

    // 公共访问方法
    public static CacheManager getInstance() {
        return instance;
    }

}
