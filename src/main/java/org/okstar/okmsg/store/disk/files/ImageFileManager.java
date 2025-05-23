package org.okstar.okmsg.store.disk.files;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.okstar.okmsg.store.disk.cache.CacheManager;
import org.okstar.okmsg.store.disk.utils.ImageComparator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import eu.siacs.conversations.Config;

public class ImageFileManager extends FileManager{

    // 直接初始化一个实例对象
    private static final ImageFileManager instance = new ImageFileManager();

    // 公共访问方法
    public static ImageFileManager getInstance() {
        return instance;
    }

    // 私有构造函数，防止外部实例化
    private ImageFileManager() {
       super();
    }

    @Override
    protected String createFile() {
        return "/images";
    }

    @Override
    public void saveFile(File file) {

    }

    @Override
    public void deleteFile(File file) {

    }

    public boolean saveImageUri(Context context, Uri uriPath) {
        final Context appContext = context.getApplicationContext();
        fileOperationExecutor.execute(() -> {
            try {
                File[] queryAllFiles = queryAllFiles();
                boolean isSameImage = false;
                for(File file : queryAllFiles) {
                    isSameImage = ImageComparator.isSameImage(appContext,uriPath,file.getAbsolutePath());
                    if(isSameImage) {
                        Log.d(Config.LOGTAG, "有相同的图片了: " + file.getAbsolutePath());
                        break;
                    }
                }

                //判断是否有相同的图片，有的话返回false，不需要保存图片
                if (isSameImage) {
                    return;
                }

                // 1. 获取 ContentResolver
                ContentResolver contentResolver = appContext.getContentResolver();

                // 2. 打开输入流
                InputStream inputStream = contentResolver.openInputStream(uriPath);
                if (inputStream == null) {
                    return;
                }

                String fileName;
                if(!uriPath.getLastPathSegment().isEmpty()) {
                    if (uriPath.getLastPathSegment() == null || !uriPath.getLastPathSegment().matches(".*\\.(jpg|jpeg|png|gif|bmp)$")) {
                        fileName = "okmsg_img_" + uriPath.getLastPathSegment() + ".png";
                    }else {
                        //如果文件有后缀就直接获取
                        fileName = "okmsg_img_" + uriPath.getLastPathSegment();
                    }
                }else {
                    fileName = "okmsg_img_" + System.currentTimeMillis() + ".png";
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

                // 6. 返回保存成功
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return false;
    }


}
