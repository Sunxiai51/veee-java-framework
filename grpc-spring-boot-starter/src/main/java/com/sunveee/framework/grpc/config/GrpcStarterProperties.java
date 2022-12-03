package com.sunveee.framework.grpc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(GrpcStarterProperties.PREFIX)
public class GrpcStarterProperties {

    public static final String PREFIX = "veee.grpc.starter";

    public static final String GRPC_STARTER_ENABLED = PREFIX + ".enabled";

    private boolean enabled = true;

    private GrpcStarterServerInterceptorProperties serverInterceptor = new GrpcStarterServerInterceptorProperties();
    private GrpcStarterClientInterceptorProperties clientInterceptor = new GrpcStarterClientInterceptorProperties();


}
