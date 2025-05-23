package org.okstar.okmsg.store.disk.files;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.okstar.okmsg.store.disk.utils.ImageComparator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

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

    public boolean saveVideoUri(Context context,final Uri uriPath,String  mimeType) {
        // 使用 Application Context
        final Context appContext = context.getApplicationContext();
        fileOperationExecutor.execute(() -> {
            try {
                queryAllFiles();

                // 1. 获取 ContentResolver
                ContentResolver contentResolver = appContext.getContentResolver();

                // 2. 打开输入流
                InputStream inputStream = contentResolver.openInputStream(uriPath);
                if (inputStream == null) {
                    return;
                }

                // 获取文件名并替换冒号为下划线
                String lastPathSegment = uriPath.getLastPathSegment();
                if (lastPathSegment != null) {
                    lastPathSegment = lastPathSegment.replaceAll("[\\\\/:*?\"<>|]", "_");
                }

                // 从 mimeType 中提取后缀
                String fileExtension = null;
                if (mimeType != null && mimeType.startsWith("video/")) {
                    fileExtension = mimeType.substring("video/".length());
                }

                String fileName;
                if (lastPathSegment != null && !lastPathSegment.isEmpty()) {
                    // 如果 mimeType 提供了后缀，则使用 mimeType 的后缀
                    if (fileExtension != null) {
                        fileName = "okmsg_video_" + lastPathSegment + "." + fileExtension;
                    } else if (!lastPathSegment.matches(".*\\.(mp4|avi|mkv|mov|wmv|flv)$")) {
                        // 如果没有 mimeType 后缀且文件名没有音频后缀，则添加默认后缀 .mp3
                        fileName = "okmsg_video_" + lastPathSegment + ".mp4";
                    } else {
                        // 如果文件有后缀就直接获取
                        fileName = "okmsg_video_" + lastPathSegment;
                    }
                } else {
                    // 如果文件名为空，使用时间戳作为文件名，并优先使用 mimeType 的后缀
                    if (fileExtension != null) {
                        fileName = "okmsg_video_" + System.currentTimeMillis() + "." + fileExtension;
                    } else {
                        fileName = "okmsg_video_" + System.currentTimeMillis() + ".mp4";
                    }
                }

                // 3. 创建目标文件
                File outputFile = new File(getChildFile(), fileName); // 保存到缓存目录
                OutputStream outputStream = new FileOutputStream(outputFile);

                // 4. 将输入流写入文件
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                // 5. 关闭流
                outputStream.close();
                inputStream.close();

            } catch (Exception e) {
               e.printStackTrace();
            }
        });

       return false;

    }


    @Override
    public void deleteFile(File file) {

    }


}
