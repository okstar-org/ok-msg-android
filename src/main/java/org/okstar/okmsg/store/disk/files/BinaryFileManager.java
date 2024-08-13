package org.okstar.okmsg.store.disk.files;

import android.util.Log;

import java.io.File;

import eu.siacs.conversations.Config;

public class BinaryFileManager extends FileManager{

    // 直接初始化一个实例对象
    private static final BinaryFileManager instance = new BinaryFileManager();

    // 公共访问方法
    public static BinaryFileManager getInstance() {
        return instance;
    }

    // 私有构造函数，防止外部实例化
    private BinaryFileManager() {
       super();
        Log.d(Config.LOGTAG,"binary :"+childFile.getAbsolutePath());
    }

    @Override
    protected String createFile() {
        return "/files";
    }

    @Override
    public void saveFile(File file) {

    }

    @Override
    public void deleteFile(File file) {

    }


}
