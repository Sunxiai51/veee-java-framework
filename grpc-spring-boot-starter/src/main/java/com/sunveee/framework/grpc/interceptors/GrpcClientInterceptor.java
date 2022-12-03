package com.sunveee.framework.grpc.interceptors;

import com.google.protobuf.Message;
import com.sunveee.framework.common.utils.json.JSONUtils;
import com.sunveee.framework.common.utils.protobuf.ProtobufJsonUtils;
import com.sunveee.framework.grpc.common.GrpcStarterConstants;
import com.sunveee.framework.grpc.config.GrpcStarterClientInterceptorProperties;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.UUID;

@Slf4j
public class GrpcClientInterceptor implements ClientInterceptor {
    private GrpcStarterClientInterceptorProperties properties;

    public GrpcClientInterceptor(GrpcStarterClientInterceptorProperties properties) {
        this.properties = properties;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        // 获取当前线程的 THREAD_ID 作为 threadTraceId
        String threadTraceId = MDC.get(GrpcStarterConstants.MDC_THREAD_ID);
        if (StringUtils.isBlank(threadTraceId)) {
            threadTraceId = UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
            log.info("[GRPC] Generated new THREAD_ID[{}] for gPRC calls.", threadTraceId);
            MDC.put(GrpcStarterConstants.MDC_THREAD_ID, threadTraceId);
        }
        final String traceId = threadTraceId;

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            public void sendMessage(ReqT message) {
                if (properties.isPrintRequestMessage()) {
                    String messageStr;
                    if (message instanceof Message) {
                        messageStr = ProtobufJsonUtils.toJsonString((Message) message);
                    } else {
                        messageStr = JSONUtils.toJSONString(message);
                    }
                    log.info("[TRACE][GRPC] {} call {}, request message: {}", properties.getName(), method.getFullMethodName(), messageStr);
                }
                super.sendMessage(message);
            }

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(GrpcStarterConstants.GRPC_METADATA_KEY_TRACE_ID, traceId);
                headers.put(GrpcStarterConstants.GRPC_METADATA_KEY_TRACE_SOURCE, properties.getName());
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onMessage(RespT message) {
                        if (properties.isPrintResponseMessage()) {
                            String messageStr;
                            if (message instanceof Message) {
                                messageStr = ProtobufJsonUtils.toJsonString((Message) message);
                            } else {
                                messageStr = JSONUtils.toJSONString(message);
                            }
                            log.info("[TRACE][GRPC] {} call {}, response message: {}", properties.getName(), method.getFullMethodName(), messageStr);
                        }
                        super.onMessage(message);
                    }

                    @Override
                    public void onHeaders(Metadata headers) {
                        if (properties.isPrintResponseHeader()) {
                            log.info("[GRPC] Response headers: {}", headers);
                        }
                        super.onHeaders(headers);
                    }

                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        if (!status.isOk()) {
                            log.error("[TRACE][GRPC] {} call {}，response NOT_OK, status: {}, trailers: {}", properties.getName(), method.getFullMethodName(), status, trailers);
                        }
                        super.onClose(status, trailers);
                    }
                }, headers);
            }
        };
    }

}
