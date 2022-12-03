package com.sunveee.framework.grpc.autoconfigure;

import com.sunveee.framework.grpc.aspect.GrpcStubCallExecuteAspect;
import com.sunveee.framework.grpc.config.GrpcStarterProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GrpcStarterProperties.class)
public class GrpcStarterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(GrpcStarterProperties.GRPC_STARTER_ENABLED)
    @ConditionalOnClass(name = "net.devh.boot.grpc.client.inject.GrpcClientBeanPostProcessor")
    public GrpcStubCallExecuteAspect grpcStubCallExecuteAspect() {
        return new GrpcStubCallExecuteAspect();
    }
}
