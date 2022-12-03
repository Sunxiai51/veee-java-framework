package com.sunveee.framework.common.utils.test;

import com.sunveee.framework.common.utils.datetime.LocalDateTimeTransferUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LocalDateTimeTransferUtilsTest
 *
 * @author SunVeee
 * @date 2022/8/24 14:33
 */
public class LocalDateTimeTransferUtilsTest {

    public static void main(String[] args) {
        // test1();
        test2();
    }

    private static void test1() {
        long now = System.currentTimeMillis();
        System.out.println(now);
        LocalDateTime localDateTime = LocalDateTimeTransferUtils.fromTimestamp(now);
        System.out.println(localDateTime);
        long now2 = LocalDateTimeTransferUtils.toTimestamp(localDateTime);
        System.out.println(now2);
    }

    private static void test2() {
        String timeStr = "2022-11-23 11:32:27";
        LocalDateTime time = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println(LocalDateTimeTransferUtils.toTimestamp(time));
    }
}
