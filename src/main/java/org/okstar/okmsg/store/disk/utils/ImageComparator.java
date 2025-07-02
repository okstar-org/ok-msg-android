package org.okstar.okmsg.store.disk.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.security.MessageDigest;

public class ImageComparator {

    /**
     * 判断两个文件是否相同（基于内容哈希）
     */
    public static boolean isSameImage(Context context, Uri contentUri, String filePath) {
        // 1. 计算 content:// URI 的文件哈希
        String contentHash = getFileHashFromUri(context, contentUri);
        if (contentHash == null) {
            Log.e("ImageComparator", "Failed to get hash from content URI");
            return false;
        }

        // 2. 计算 /data/ 文件的哈希
        String fileHash = getFileHashFromPath(filePath);
        if (fileHash == null) {
            Log.e("ImageComparator", "Failed to get hash from file path");
            return false;
        }

        // 3. 比对哈希值
        return contentHash.equals(fileHash);
    }

    /**
     * 从 content:// URI 获取文件哈希（MD5）
     */
    private static String getFileHashFromUri(Context context, Uri uri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream != null) {
                return calculateHash(inputStream);
            }
        } catch (Exception e) {
            Log.e("ImageComparator", "Error reading content URI", e);
        }
        return null;
    }

    /**
     * 从文件路径获取文件哈希（MD5）
     */
    private static String getFileHashFromPath(String filePath) {
        try (InputStream inputStream = new File(filePath).toURI().toURL().openStream()) {
            return calculateHash(inputStream);
        } catch (Exception e) {
            Log.e("ImageComparator", "Error reading file path", e);
        }
        return null;
    }

    /**
     * 计算输入流的 MD5 哈希
     */
    private static String calculateHash(InputStream inputStream) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }
        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}