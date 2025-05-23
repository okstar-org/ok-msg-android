package org.okstar.okmsg.store.disk.files;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import eu.siacs.conversations.Config;

public class AudioFileManager extends FileManager{

    // 直接初始化一个实例对象
    private static final AudioFileManager instance = new AudioFileManager();

    // 公共访问方法
    public static AudioFileManager getInstance() {
        return instance;
    }

    // 私有构造函数，防止外部实例化
    private AudioFileManager() {
       super();
        Log.d(Config.LOGTAG,"audio :"+childFile.getAbsolutePath());
    }

    @Override
    protected String createFile() {
        return "/audio";
    }

    @Override
    public void saveFile(File file) {

    }

    public void saveAudioFile(Context context,Uri uri,String mimeType) {
        final Context appContext = context.getApplicationContext();
        fileOperationExecutor.execute(() -> {
            try {
                queryAllFiles();

                // 1. 获取 ContentResolver
                ContentResolver contentResolver = appContext.getContentResolver();

                // 2. 打开输入流
                InputStream inputStream = contentResolver.openInputStream(uri);
                if (inputStream == null) {
                    return;
                }

                // 获取文件名并替换冒号为下划线
                String lastPathSegment = uri.getLastPathSegment();
                if (lastPathSegment != null) {
                    lastPathSegment = lastPathSegment.replaceAll("[\\\\/:*?\"<>|]", "_");
                }

                // 从 mimeType 中提取后缀
                String fileExtension = null;
                if (mimeType != null && mimeType.startsWith("audio/")) {
                    fileExtension = mimeType.substring("audio/".length());
                }

                String fileName;
                if (lastPathSegment != null && !lastPathSegment.isEmpty()) {
                    // 如果 mimeType 提供了后缀，则使用 mimeType 的后缀
                    if (fileExtension != null) {
                        fileName = "okmsg_audio_" + lastPathSegment + "." + fileExtension;
                    } else if (!lastPathSegment.matches(".*\\.(mp3|wav|ogg|aac|flac)$")) {
                        // 如果没有 mimeType 后缀且文件名没有音频后缀，则添加默认后缀 .mp3
                        fileName = "okmsg_audio_" + lastPathSegment + ".mp3";
                    } else {
                        // 如果文件有后缀就直接获取
                        fileName = "okmsg_audio_" + lastPathSegment;
                    }
                } else {
                    // 如果文件名为空，使用时间戳作为文件名，并优先使用 mimeType 的后缀
                    if (fileExtension != null) {
                        fileName = "okmsg_audio_" + System.currentTimeMillis() + "." + fileExtension;
                    } else {
                        fileName = "okmsg_audio_" + System.currentTimeMillis() + ".mp3";
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

                Log.d(Config.LOGTAG, "音频文件保存成功: " + outputFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(Config.LOGTAG, "保存音频文件失败", e);
            }

        });
    }


    @Override
    public void deleteFile(File file) {

    }


}
