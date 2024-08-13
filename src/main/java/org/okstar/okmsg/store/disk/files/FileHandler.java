package org.okstar.okmsg.store.disk.files;

import java.io.File;

public interface FileHandler {

    /**
     * 子文件夹保存文件
     * @param file
     */
    void saveFile(File file);

    /**
     * 子文件夹删除文件
     * @param file
     */
    void deleteFile(File file);

}
