package com.sunveee.framework.logbackloader.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(LogbackLoaderProperties.PREFIX)
public class LogbackLoaderProperties {

    public static final String PREFIX = "logbackloader";

    public static final String LOGBACK_LOADER_ENABLED = PREFIX + ".enabled";

    /**
     * 初始化类型
     */
    public static final String LOGBACK_LOADER_INITIALIZE_TYPE = PREFIX + ".initialize.type";
    public static final String LOGBACK_LOADER_INITIALIZE_NAME_PLACEHOLDER = PREFIX + ".initialize.name.placeholder";
    public static final String LOGBACK_LOADER_INITIALIZE_NAME = PREFIX + ".initialize.name"; // 初始化名称，用于${logName}
    public static final String LOGBACK_LOADER_INITIALIZE_APOLLO_KV_NAMESPACE = PREFIX + ".initialize.apollo.kv.namespace";
    public static final String LOGBACK_LOADER_INITIALIZE_APOLLO_KV_KEY = PREFIX + ".initialize.apollo.kv.key";
    public static final String LOGBACK_LOADER_INITIALIZE_APOLLO_XML_NAMESPACE = PREFIX + ".initialize.apollo.xml.namespace";
    public static final String LOGBACK_LOADER_INITIALIZE_LOCAL_FILE_PATH = PREFIX + ".initialize.localfile.path";
    public static final String LOGBACK_LOADER_INITIALIZE_NACOS_SERVER_ADDR = PREFIX + ".initialize.nacos.server-addr";
    public static final String LOGBACK_LOADER_INITIALIZE_NACOS_XML_NAMESPACE = PREFIX + ".initialize.nacos.namespace";
    public static final String LOGBACK_LOADER_INITIALIZE_NACOS_DATA_ID = PREFIX + ".initialize.nacos.data-id";
    public static final String LOGBACK_LOADER_INITIALIZE_NACOS_GROUP = PREFIX + ".initialize.nacos.group";


    /**
     * 是否启用
     */
    private boolean enabled = true;


}
