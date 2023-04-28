package com.sunveee.framework.logbackloader.common.enums;


import org.apache.commons.lang3.StringUtils;

/**
 * LogbackConfigInitializeType
 *
 * @author SunVeee
 * @date 2022/2/17 15:20
 */
public enum LogbackConfigInitializeType {
    LOCAL_FILE,
    APOLLO_XML_CONFIG,
    APOLLO_KV_CONFIG,
    NACOS_CONFIG,

    ;

    public static LogbackConfigInitializeType nullableParse(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        try {
            return LogbackConfigInitializeType.valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
