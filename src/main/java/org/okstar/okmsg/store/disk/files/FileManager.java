package org.okstar.okmsg.store.disk.files;



import static eu.siacs.conversations.BuildConfig.LOGTAG;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.siacs.conversations.BuildConfig;
import eu.siacs.conversations.Config;

/**
 * /data/user/0/org.okstar.okmsg/files
 */
public class FileManager implements FileHandler{

    // 直接初始化一个实例对象
    private final File filesDirs;

    protected File childFile;

    // 创建一个固定大小的线程池(可根据需要调整大小)
    protected static final ExecutorService fileOperationExecutor =
            Executors.newFixedThreadPool(4); // 通常4个线程足够

    private static final FileManager instance = new FileManager();

    // 公共访问方法
    public static FileManager getInstance() {
        return instance;
    }


    // 私有构造函数，防止外部实例化
    FileManager() {
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
    protected  String createFile(){
        return "/okmsg_files";
    };


    /**
     * 获取文件大小  字节为单位
     * @return
     */
    public long getFilesCount(){
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
                Log.d(LOGTAG, "queryAllFiles: " + file.getAbsolutePath());
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
        Log.d(LOGTAG, "queryFile: " + selectedFile.getAbsolutePath());
    }

    // 格式化文件大小
    public String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    // 获取内部存储总容量
    public long getTotalInternalStorageSize() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return totalBlocks * blockSize; // 返回字节数
    }

    // 获取可用内部存储空间
    public long getAvailableInternalStorageSize() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }


    public String calculateStoragePercentage() {
        long totalStorage = getTotalInternalStorageSize();
        if(totalStorage == 0 || getFilesCount() == 0) {
            return "0.0%";
        }

        double radio = ((double) getFilesCount() / (double) totalStorage) * 100;
        if(radio <=0) {
            return "0.0%";
        }

        if (radio >= 1.0) {
            return String.format(Locale.getDefault(), "%.2f%%", radio);
        } else if (radio >= 0.001) {
            return String.format(Locale.getDefault(), "%.4f%%", radio);
        } else {
            // 极小值，用科学计数法或直接显示 "接近0%"
            return "< 0.001%";
        }
    }

    @Override
    public void saveFile(File file) {

    }

    @Override
    public void deleteFile(File file) {

    }
}
