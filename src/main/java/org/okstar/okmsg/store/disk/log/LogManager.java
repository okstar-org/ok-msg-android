package org.okstar.okmsg.store.disk.log;

import android.util.Log;

import org.okstar.okmsg.utils.DateFormatUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import eu.siacs.conversations.Config;

public class LogManager {

    // 直接初始化一个实例对象
    private static final LogManager instance = new LogManager();
    //根据月份区分目录
    private static File currentMonthFile;

    // 私有构造函数，防止外部实例化
    private LogManager() {
        File logFile = new File(Config.application.getFilesDir() + "/logs");
        if (!logFile.exists()) {
            logFile.mkdirs();
        }
        Log.d(Config.LOGTAG, "logDirs :" + logFile.getAbsolutePath());
        currentMonthFile = new File(logFile.getAbsolutePath() + "/" + DateFormatUtils.getCurrentYearMonth());
        if (!currentMonthFile.exists()) {
            currentMonthFile.mkdirs();
        }
        Log.d(Config.LOGTAG, "logDirs->currentMonthFile :" + currentMonthFile.getAbsolutePath());
    }

    // 公共访问方法
    public static LogManager getInstance() {
        return instance;
    }

    /**
     * 写入log
     *
     * @param logValue
     */
    public static void writeToLog(String logValue) {
        File logFile = new File(currentMonthFile + "/" + System.currentTimeMillis() + ".log");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (logFile.exists()) {
            Log.d(Config.LOGTAG, "logFile :" + logFile.getAbsolutePath());
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile))) {
                writer.write(logValue);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除全部日志
     */
    public static void deleteAllLogs() {
        File logFile = new File(Config.application.getFilesDir() + "/logs");
        if (logFile.exists()) {
            try {
                for (File file : logFile.listFiles()) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


}
