package com.sunveee.framework.grpc.config;

import lombok.Data;

/**
 * GrpcStarterClientInterceptorProperties
 *
 * @author SunVeee
 * @date 2022/3/25 21:00
 */
@Data
public class GrpcStarterClientInterceptorProperties {
    private String name = "undefined name";
    private boolean printRequestMessage = true;
    private boolean printResponseMessage = true;
    private boolean printResponseHeader = false;
    private boolean verbose = false;
}
