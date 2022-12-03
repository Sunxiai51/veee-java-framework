package com.sunveee.framework.grpc.common;

import io.grpc.Metadata;

/**
 * GrpcStarterConstants
 *
 * @author SunVeee
 * @date 2022/3/25 14:29
 */
public class GrpcStarterConstants {
    public static final String MDC_THREAD_ID = "THREAD_ID";

    public static final String GRPC_METADATA_TRACE_ID = "trace_id";
    public static final String GRPC_METADATA_TRACE_SOURCE = "trace_source";
    public static final Metadata.Key<String> GRPC_METADATA_KEY_TRACE_ID = Metadata.Key.of(GRPC_METADATA_TRACE_ID, Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> GRPC_METADATA_KEY_TRACE_SOURCE = Metadata.Key.of(GRPC_METADATA_TRACE_SOURCE, Metadata.ASCII_STRING_MARSHALLER);

    public static final String GRPC_METADATA_ERROR_RESPONSE = "error_response";
    public static final Metadata.Key<String> GRPC_METADATA_KEY_ERROR_RESPONSE = Metadata.Key.of(GRPC_METADATA_ERROR_RESPONSE, Metadata.ASCII_STRING_MARSHALLER);

}
