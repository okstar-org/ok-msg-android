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

    public void saveDocFile(Context context,final Uri uri) {
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

                String fileName;
                if (lastPathSegment != null && !lastPathSegment.isEmpty()) {
                    // 判断是否有文档文件后缀，如果没有则添加默认后缀 .txt
                    if (!lastPathSegment.matches(".*\\.(doc|docx|xlsx|xls|txt|pdf)$")) {
                        fileName = "okmsg_doc_" + lastPathSegment + ".txt";
                    } else {
                        // 如果文件有后缀就直接获取
                        fileName = "okmsg_doc_" + lastPathSegment;
                    }
                } else {
                    // 如果文件名为空，使用时间戳作为文件名
                    fileName = "okmsg_doc_" + System.currentTimeMillis() + ".txt";
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

                Log.d(Config.LOGTAG, "文档文件保存成功: " + outputFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(Config.LOGTAG, "保存文档文件失败", e);
            }
        });

    }

    @Override
    public void deleteFile(File file) {

    }

    public String getFileCountFormat() {
        long count = getFilesCount();
        return formatFileSize(count);
    }

}
