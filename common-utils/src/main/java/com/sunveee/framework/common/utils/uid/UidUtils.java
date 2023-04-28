package com.sunveee.framework.common.utils.uid;

import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * UidUtils
 *
 * @author SunVeee
 * @date 2022/10/20 11:40
 */
public class UidUtils {

    /**
     * 返回替换了{@code -}的UUID
     *
     * @return uuid
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 返回使用指定前缀和时间信息和随机4位大写字母后缀的编号
     *
     * @param prefix 前缀，比如DA
     * @param time   时间
     * @return 编号，比如DA20221014164952359COME
     */
    public static String timeCode(String prefix, LocalDateTime time) {
        return String.join("",
                prefix,
                time.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")),
                RandomStringUtils.randomAlphabetic(4).toUpperCase()
        );
    }
}
