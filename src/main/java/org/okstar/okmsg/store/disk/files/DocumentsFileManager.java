package org.okstar.okmsg.store.disk.files;

import android.util.Log;

import java.io.File;

import eu.siacs.conversations.Config;

public class DocumentsFileManager extends FileManager{

    // 直接初始化一个实例对象
    private static final DocumentsFileManager instance = new DocumentsFileManager();

    // 公共访问方法
    public static DocumentsFileManager getInstance() {
        return instance;
    }

    // 私有构造函数，防止外部实例化
    private DocumentsFileManager() {
       super();
        Log.d(Config.LOGTAG,"documents :"+childFile.getAbsolutePath());
    }

    @Override
    protected String createFile() {
        return "/documents";
    }

    @Override
    public void saveFile(File file) {

    }

    @Override
    public void deleteFile(File file) {

    }


}
