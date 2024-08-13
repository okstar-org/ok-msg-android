package org.okstar.okmsg.store.disk.files;

import android.util.Log;

import java.io.File;

import eu.siacs.conversations.Config;

public class VideoFileManager extends FileManager{

    // 直接初始化一个实例对象
    private static final VideoFileManager instance = new VideoFileManager();

    // 公共访问方法
    public static VideoFileManager getInstance() {
        return instance;
    }

    // 私有构造函数，防止外部实例化
    private VideoFileManager() {
       super();
        Log.d(Config.LOGTAG,"video :"+childFile.getAbsolutePath());
    }

    @Override
    protected String createFile() {
        return "/video";
    }

    @Override
    public void saveFile(File file) {

    }

    @Override
    public void deleteFile(File file) {

    }


}
