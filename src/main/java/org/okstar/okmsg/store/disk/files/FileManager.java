package org.okstar.okmsg.store.disk.files;


import android.util.Log;

import java.io.File;

import eu.siacs.conversations.Config;

/**
 * /data/user/0/org.okstar.okmsg/files
 */
public abstract class FileManager implements FileHandler{

    // 直接初始化一个实例对象
    private final File filesDirs;

    protected File childFile;

    // 私有构造函数，防止外部实例化
    public FileManager() {
        filesDirs = Config.application.getFilesDir();
        childFile = new File(filesDirs.getAbsoluteFile() + createFile());
        if(!childFile.exists()) {
            childFile.mkdirs();
        }
    }

    protected File getFilePath(){
        return filesDirs;
    }

    /**
     * 创建子文件夹
     * @return
     */
    protected abstract String createFile();

}
