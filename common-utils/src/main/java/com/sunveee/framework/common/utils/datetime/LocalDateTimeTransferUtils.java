package com.sunveee.framework.common.utils.datetime;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * LocalDateTimeTransferUtils
 *
 * @author SunVeee
 * @date 2022/8/24 14:20
 */
public class LocalDateTimeTransferUtils {

    public static LocalDateTime fromTimestamp(long timestamp, ZoneOffset zoneOffset) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(timestamp / 1000, 1000000 * ((long) timestamp % 1000)), zoneOffset);
    }

    public static LocalDateTime fromTimestamp(long timestamp) {
        return fromTimestamp(timestamp, systemZoneOffset());
    }

    public static long toTimestamp(LocalDateTime localDateTime, ZoneOffset zoneOffset) {
        return localDateTime.toInstant(zoneOffset).toEpochMilli();
    }

    public static long toTimestamp(LocalDateTime localDateTime) {
        return toTimestamp(localDateTime, systemZoneOffset());
    }

    private static ZoneOffset systemZoneOffset() {
        return ZoneOffset.systemDefault().getRules().getOffset(Instant.now());
    }
}
