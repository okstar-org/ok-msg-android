package org.okstar.okmsg;

import androidx.multidex.MultiDexApplication;

import org.okstar.okmsg.store.disk.cache.CacheManager;
import org.okstar.okmsg.store.disk.files.AudioFileManager;
import org.okstar.okmsg.store.disk.files.BinaryFileManager;
import org.okstar.okmsg.store.disk.files.DocumentsFileManager;
import org.okstar.okmsg.store.disk.files.ImageFileManager;
import org.okstar.okmsg.store.disk.files.VideoFileManager;
import org.okstar.okmsg.store.disk.log.LogManager;
import org.okstar.okmsg.store.mem.StoreManager;

public class OkMsgApplication extends MultiDexApplication {

    public static OkMsgApplication okMsgApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        okMsgApplication = this;

        //初始化mmkv缓存目录
        StoreManager.getInstance();
        //初始化日志目录，缓存目录，文件目录
        LogManager.getInstance();
        CacheManager.getInstance();
        ImageFileManager.getInstance();
        VideoFileManager.getInstance();
        AudioFileManager.getInstance();
        DocumentsFileManager.getInstance();
        BinaryFileManager.getInstance();

    }

}
