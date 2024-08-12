package org.okstar.okmsg;

import androidx.multidex.MultiDexApplication;

import eu.siacs.conversations.mem.StoreManager;

public class OkMsgApplication extends MultiDexApplication {

    public static OkMsgApplication okMsgApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        okMsgApplication = this;

        //初始化mmkv缓存目录
        StoreManager.getInstance();
    }

}
