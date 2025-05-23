package org.okstar.okmsg.store.disk.files;


import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.siacs.conversations.BuildConfig;
import eu.siacs.conversations.Config;

/**
 * /data/user/0/org.okstar.okmsg/files
 */
public abstract class FileManager implements FileHandler{

    // 直接初始化一个实例对象
    private final File filesDirs;

    protected File childFile;

    // 创建一个固定大小的线程池(可根据需要调整大小)
    protected static final ExecutorService fileOperationExecutor =
            Executors.newFixedThreadPool(4); // 通常4个线程足够

    // 私有构造函数，防止外部实例化
    public FileManager() {
        filesDirs = Config.application.getFilesDir();
        childFile = new File(filesDirs.getAbsoluteFile() + createFile());
        if(!childFile.exists()) {
            childFile.mkdirs();
        }
    }

    public File getFilePath(){
        return filesDirs;
    }

    public File getChildFilePath() {
        return childFile.getAbsoluteFile();
    }

    public File getChildFile() {
        return childFile;
    }

    /**
     * 创建子文件夹
     * @return
     */
    protected abstract String createFile();


    /**
     * 获取文件大小  字节为单位
     * @return
     */
    public int getFilesCount(){
        int fileLength = 0;
        try {
            if(getChildFile().exists()) {
                for (File file: Objects.requireNonNull(getChildFile().listFiles())) {
                    fileLength += file.length();
                }
                return fileLength;
            }else {
                return 0;
            }
        }catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 保存文件
     * @param filePath
     * @param bytesData
     */
    public void saveFile(String filePath,byte[] bytesData) {
        String tempPath;
        if (filePath == null || filePath.trim().isEmpty()) {
            tempPath = getChildFilePath() + "/" + System.currentTimeMillis();
        }else {
            tempPath = getChildFilePath() + "/" + filePath;
        }
        filePath = tempPath;

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            fos.write(bytesData);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *  删除所有文件
     */
    public void deleteAllFiles() {
        try {
            if(getChildFile().exists()) {
                if(getChildFile().listFiles()!=null) {
                    for (File file : Objects.requireNonNull(getChildFile().listFiles())) {
                        file.delete();
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除文件
     * @param filePath
     */
    public void deleteFile(String filePath){
        if(getChildFile().exists())  {
            for (File file : Objects.requireNonNull(getChildFile().listFiles())) {
                if(file.getName().equals(filePath)){
                    file.delete();
                    break;
                }
            }
        }
    }

    public File[] queryAllFiles(){
        if(getChildFile().exists())  {
            for (File file : Objects.requireNonNull(getChildFile().listFiles())) {
                Log.d(BuildConfig.LOGTAG, "queryAllFiles: " + file.getAbsolutePath());
            }
            return Objects.requireNonNull(getChildFile().listFiles());
        }
        return null;
    }


    public void queryFile(String filePath) {
        File selectedFile = null;
        for(File itemFile:  Objects.requireNonNull(getChildFile().listFiles())) {
            if(itemFile.getName().equals(filePath)){
                selectedFile = itemFile;
                break;
            }
        }
        Log.d(BuildConfig.LOGTAG, "queryFile: " + selectedFile.getAbsolutePath());
    }



}
