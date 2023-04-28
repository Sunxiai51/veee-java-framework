package com.sunveee.framework.common.utils.protobuf;

import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * TimestampTransferUtils
 *
 * @author SunVeee
 * @date 2022/3/3 11:51
 */
public class ProtoTimestampUtils {

    public static Timestamp toTimestampUseDefaultZone(LocalDateTime localDateTime) {
        return toTimestamp(localDateTime, ZoneId.systemDefault());
    }


    public static Timestamp toTimestamp(LocalDateTime localDateTime, ZoneId zoneId) {
        if (null == localDateTime) {
            return null;
        }
        Instant instant = localDateTime.atZone(zoneId).toInstant();
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    public static LocalDateTime toLocalDateTimeUseDefaultZone(Timestamp timestamp) {
        return toLocalDateTime(timestamp, ZoneId.systemDefault());
    }

    public static LocalDateTime toLocalDateTime(Timestamp timestamp, ZoneId zoneId) {
        if (null == timestamp) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()), zoneId);
    }

}
