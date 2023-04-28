package com.sunveee.framework.grpc.config;

import lombok.Data;

/**
 * GrpcStarterServerInterceptorProperties
 *
 * @author SunVeee
 * @date 2022/3/25 17:02
 */
@Data
public class GrpcStarterServerInterceptorProperties {
    private boolean printRequestMessage = true;
    private boolean verbose = false;
}
