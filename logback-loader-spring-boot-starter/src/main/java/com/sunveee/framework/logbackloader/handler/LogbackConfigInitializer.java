package com.sunveee.framework.logbackloader.handler;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.sunveee.framework.logbackloader.autoconfigure.LogbackLoaderProperties;
import com.sunveee.framework.logbackloader.common.enums.LogbackConfigInitializeType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Logback配置初始化
 *
 * @author SunVeee
 * @version 2020-11-30 16:04:53
 */
@Slf4j
public class LogbackConfigInitializer {
    private static final String LOGBACK_CONFIG_LOCAL_FILEPATH = "/tmp/logback-config-cache/";

    public static void initialize() {
        // 是否启用LogbackLoader
        String logbackLoaderEnabledStr = System.getProperty(LogbackLoaderProperties.LOGBACK_LOADER_ENABLED);
        boolean logbackLoaderEnabled = StringUtils.isNotEmpty(logbackLoaderEnabledStr) && Boolean.parseBoolean(logbackLoaderEnabledStr);
        // 未启用直接退出方法
        if (!logbackLoaderEnabled) {
            log.info("LogbackLoader not enabled, exit initialize.");
            return;
        }

        // 获取日志配置
        String logbackConfig = obtainLogbackConfig();
        // 处理日志配置
        String logbackConfigFormatted = formatLogbackConfig(logbackConfig);
        // 激活日志配置
        activeLogbackConfig(logbackConfigFormatted);

        log.info("Logback config initialize success.");
    }

    private static String formatLogbackConfig(String logbackConfig) {
        String placeholder = System.getProperty(LogbackLoaderProperties.LOGBACK_LOADER_INITIALIZE_NAME_PLACEHOLDER, "_REPLACE_LOG_NAME_HERE_");
        String logName = System.getProperty(LogbackLoaderProperties.LOGBACK_LOADER_INITIALIZE_NAME, "");

        if (logbackConfig.contains(placeholder)) {
            // 当存在placeholder时，校验logName不能为空
            Assert.isTrue(StringUtils.isNotBlank(logName), String.format("require log name to replace %s, check the config of %s", placeholder, LogbackLoaderProperties.LOGBACK_LOADER_INITIALIZE_NAME));
        }

        // 替换logName
        String replacedLogbackXmlConfig = logbackConfig.replaceAll(placeholder, logName);

        return replacedLogbackXmlConfig;
    }

    private static String obtainLogbackConfig() {
        // 获取初始化类型
        String initializeType = System.getProperty(LogbackLoaderProperties.LOGBACK_LOADER_INITIALIZE_TYPE);
        LogbackConfigInitializeType type = LogbackConfigInitializeType.nullableParse(initializeType);
        Assert.notNull(type, "unknown initialize type, check the config of " + LogbackLoaderProperties.LOGBACK_LOADER_INITIALIZE_TYPE);
        switch (type) {
            case LOCAL_FILE:
                return obtainLogbackConfigFromLocalFile();
            case APOLLO_KV_CONFIG:
                return obtainLogbackConfigFromApolloKeyValue();
            case APOLLO_XML_CONFIG:
                return obtainLogbackConfigFromApolloXml();
            case NACOS_CONFIG:
                return obtainLogbackConfigFromNacos();
            default:
                throw new IllegalArgumentException("Unsupported LogbackConfigInitializeType: " + type);
        }
    }

    private static String obtainLogbackConfigFromNacos() {
        String serverAddr = System.getProperty(LogbackLoaderProperties.LOGBACK_LOADER_INITIALIZE_NACOS_SERVER_ADDR);
        Assert.isTrue(StringUtils.isNotBlank(serverAddr), "cannot get nacos serverAddr");
        String namespace = System.getProperty(LogbackLoaderProperties.LOGBACK_LOADER_INITIALIZE_NACOS_XML_NAMESPACE);
        Assert.isTrue(StringUtils.isNotBlank(namespace), "cannot get nacos logback config namespace");
        String dataId = System.getProperty(LogbackLoaderProperties.LOGBACK_LOADER_INITIALIZE_NACOS_DATA_ID, "common-logback-config");
        String group = System.getProperty(LogbackLoaderProperties.LOGBACK_LOADER_INITIALIZE_NACOS_GROUP, "COMMON_GROUP");
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
        properties.put(PropertyKeyConst.NAMESPACE, namespace);
        try {
            com.alibaba.nacos.api.config.ConfigService configService = NacosFactory.createConfigService(properties);
            String nacosLogbackConfig = configService.getConfig(dataId, group, 5000);
            Assert.isTrue(StringUtils.isNotBlank(nacosLogbackConfig), "cannot obtain logback config from nacos");

            System.out.println(nacosLogbackConfig);
            return nacosLogbackConfig;
        } catch (NacosException e) {
            throw new RuntimeException("obtain logback config from nacos exception", e);
        }
    }

    private static String obtainLogbackConfigFromApolloXml() {
        String apolloEnabledStr = System.getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED);
        Assert.isTrue(StringUtils.isNotEmpty(apolloEnabledStr) && Boolean.parseBoolean(apolloEnabledStr), "cannot obtain logback config: apollo not enabled");

        String apolloNamespace = System.getProperty(LogbackLoaderProperties.LOGBACK_LOADER_INITIALIZE_APOLLO_XML_NAMESPACE, "logback-config");

        // 从apollo读取日志配置
        ConfigFile configFile = ConfigService.getConfigFile(apolloNamespace, ConfigFileFormat.XML);
        String apolloLogbackConfig = configFile.getContent();
        Assert.isTrue(StringUtils.isNotBlank(apolloLogbackConfig), "cannot obtain logback config: " + String.format("no xml value match apollo namespace[%s]", apolloNamespace));

        return apolloLogbackConfig;
    }

    private static String obtainLogbackConfigFromApolloKeyValue() {
        String apolloEnabledStr = System.getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED);
        Assert.isTrue(StringUtils.isNotEmpty(apolloEnabledStr) && Boolean.parseBoolean(apolloEnabledStr), "cannot obtain logback config: apollo not enabled");

        String apolloNamespace = System.getProperty(LogbackLoaderProperties.LOGBACK_LOADER_INITIALIZE_APOLLO_KV_NAMESPACE, "common-logback-config");
        String apolloConfigKey = System.getProperty(LogbackLoaderProperties.LOGBACK_LOADER_INITIALIZE_APOLLO_KV_KEY, "default");

        // 从apollo读取日志配置
        Config config = ConfigService.getConfig(apolloNamespace);
        String apolloLogbackConfig = config.getProperty(apolloConfigKey, "");
        Assert.isTrue(StringUtils.isNotBlank(apolloLogbackConfig), "cannot obtain logback config: " + String.format("no value match apollo namespace[%s] and key[%s]", apolloNamespace, apolloConfigKey));

        return apolloLogbackConfig;
    }

    private static String obtainLogbackConfigFromLocalFile() {
        String logbackConfigFile = System.getProperty(LogbackLoaderProperties.LOGBACK_LOADER_INITIALIZE_LOCAL_FILE_PATH);
        Assert.isTrue(StringUtils.isNotBlank(logbackConfigFile), "cannot obtain logback config: require file path, check the config of " + LogbackLoaderProperties.LOGBACK_LOADER_INITIALIZE_LOCAL_FILE_PATH);

        File file = new File(logbackConfigFile);
        byte[] fileBytes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileBytes);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("cannot obtain logback config: file not found at " + logbackConfigFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new String(fileBytes, StandardCharsets.UTF_8);
    }

    private static void activeLogbackConfig(String xmlStr) {
        final String filePath = LOGBACK_CONFIG_LOCAL_FILEPATH;
        final String fileName = String.join("",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")),
                "-",
                String.valueOf(Math.abs(xmlStr.hashCode())),
                "-",
                RandomStringUtils.randomAlphabetic(4).toUpperCase(),
                ".xml");
        final String fileFullPath = filePath + fileName;
        File dir = new File(filePath);
        dir.mkdirs();
        try (FileWriter fr = new FileWriter(fileFullPath)) {
            // 写入本地文件
            fr.append(xmlStr);
            fr.flush();
            // 设置系统变量，用于spring启动时的日志框架读取
            System.setProperty("logging.config", fileFullPath);

            log.info("Active logback config finish, file: {}.", fileFullPath);
        } catch (Throwable e) {
            log.error("Active Logback config exception.", e);
        }

    }

}
