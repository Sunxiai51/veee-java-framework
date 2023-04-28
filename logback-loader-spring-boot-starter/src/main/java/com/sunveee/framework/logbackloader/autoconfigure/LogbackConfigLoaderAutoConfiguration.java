package com.sunveee.framework.logbackloader.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(LogbackLoaderProperties.LOGBACK_LOADER_ENABLED)
@EnableConfigurationProperties(LogbackLoaderProperties.class)
public class LogbackConfigLoaderAutoConfiguration {

}
