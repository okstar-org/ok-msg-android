package org.okstar.okmsg.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateFormatUtils {

    /**
     * 获取当前日期的年和月，格式为 "YYYY-MM"。
     *
     * @return 当前日期的年月字符串。
     */
    public static String getCurrentYearMonth() {
        // 获取当前日期
        LocalDate now = LocalDate.now();

        // 定义年月的格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        // 格式化当前日期
        return now.format(formatter);
    }

}
