package com.sunveee.framework.grpc.interceptors;

import com.google.protobuf.Message;
import com.sunveee.framework.common.exceptions.exception.BizException;
import com.sunveee.framework.common.exceptions.exception.LogicException;
import com.sunveee.framework.common.utils.json.JSONUtils;
import com.sunveee.framework.common.utils.protobuf.ProtobufJsonUtils;
import com.sunveee.framework.grpc.common.GrpcStarterConstants;
import com.sunveee.framework.grpc.config.GrpcStarterServerInterceptorProperties;
import com.sunveee.framework.rpc.common.model.exception.ErrorResponse;
import com.sunveee.framework.rpc.common.model.response.ResponseCodeEnum;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.UUID;

@Slf4j
public class GrpcServerInterceptor implements ServerInterceptor {
    private GrpcStarterServerInterceptorProperties properties;

    public GrpcServerInterceptor(GrpcStarterServerInterceptorProperties properties) {
        this.properties = properties;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        // 从来自客户端的header中取 traceId 与 traceSource
        String traceId = headers.get(GrpcStarterConstants.GRPC_METADATA_KEY_TRACE_ID);
        String traceSource = headers.get(GrpcStarterConstants.GRPC_METADATA_KEY_TRACE_SOURCE);
        // traceId 为空时，生成新的 traceId
        if (StringUtils.isBlank(traceId)) {
            traceId = UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
            if (properties.isVerbose()) {
                log.debug("[GRPC] TraceId not found in herders from client, generate one in server: {}.", traceId);
            }
            headers.put(GrpcStarterConstants.GRPC_METADATA_KEY_TRACE_ID, traceId);
        }

        // traceId 作为 threadTraceId 放入MDC
        String threadTraceId = MDC.get(GrpcStarterConstants.MDC_THREAD_ID);
        if (StringUtils.isNotBlank(threadTraceId)) {
            log.info("[GRPC] Replace THREAD_ID from {} to {}.", threadTraceId, traceId);
        }
        MDC.put(GrpcStarterConstants.MDC_THREAD_ID, traceId);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(next.startCall(call, headers)) {
            @Override
            public void onReady() {
                // Server 收到请求时的日志打印
                if (!properties.isPrintRequestMessage()) {
                    // 当开启 printRequestMessage 时，将会在onMessage()输出日志，为了避免重复输出，这里添加了一个判断条件
                    log.info("[TRACE][GRPC] Received request from client[{}], method: {}.", traceSource, call.getMethodDescriptor().getFullMethodName());
                }
                if (properties.isVerbose()) {
                    log.debug("[GRPC] MethodDescriptor: {}, attributes: {}.", call.getMethodDescriptor(), call.getAttributes());
                }
                super.onReady();
            }

            @Override
            public void onMessage(ReqT message) {
                // 打印收到的消息
                if (properties.isPrintRequestMessage()) {
                    String messageStr;
                    try {
                        if (message instanceof Message) {
                            messageStr = ProtobufJsonUtils.toJsonString((Message) message);
                        } else {
                            messageStr = JSONUtils.toJSONString(message);
                        }
                    } catch (Throwable throwable) {
                        log.warn("[GRPC] Transfer message to log content exception.", throwable);
                        messageStr = "";
                    }
                    log.info("[TRACE][GRPC] Received request from client[{}], method: {}, message: {}", traceSource, call.getMethodDescriptor().getFullMethodName(), messageStr);
                }
                super.onMessage(message);
            }


            @Override
            public void onComplete() {
                log.info("[TRACE][GRPC] Request from client[{}] complete, method: {}", traceSource, call.getMethodDescriptor().getFullMethodName());
                super.onComplete();
                MDC.remove(GrpcStarterConstants.MDC_THREAD_ID);
            }

            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (BizException bizException) {
                    log.warn("[TRACE][GRPC] Handle request from client[{}] exception: {}, method: {}", traceSource, bizException.toString(), call.getMethodDescriptor().getFullMethodName());
                    Metadata trailers = new Metadata();
                    trailers.put(GrpcStarterConstants.GRPC_METADATA_KEY_ERROR_RESPONSE,
                            JSONUtils.toJSONString(ErrorResponse.builder()
                                    .code(ResponseCodeEnum.BIZ_EXCEPTION.getCode())
                                    .message(bizException.getMessage())
                                    .detail(bizException.getDetail())
                                    .build()));
                    call.close(Status.fromThrowable(Status.INTERNAL.withDescription(bizException.getMessage()).asRuntimeException(trailers)), trailers);
                } catch (LogicException logicException) {
                    log.warn("[TRACE][GRPC] Handle request from client[{}] exception: {}, method: {}", traceSource, logicException.toString(), call.getMethodDescriptor().getFullMethodName());
                    Metadata trailers = new Metadata();
                    trailers.put(GrpcStarterConstants.GRPC_METADATA_KEY_ERROR_RESPONSE,
                            JSONUtils.toJSONString(ErrorResponse.builder()
                                    .code(ResponseCodeEnum.LOGIC_EXCEPTION.getCode())
                                    .message(logicException.getMessage())
                                    .detail(logicException.getDetail())
                                    .build()));
                    call.close(Status.fromThrowable(Status.INTERNAL.withDescription(logicException.getMessage()).asRuntimeException(trailers)), trailers);
                } catch (Exception e) {
                    log.warn("[TRACE][GRPC] Handle request from client[{}] exception: {}, method: {}", traceSource, e.getClass().getCanonicalName(), call.getMethodDescriptor().getFullMethodName(), e);
                    // 返回
                    Metadata trailers = new Metadata();
                    trailers.put(GrpcStarterConstants.GRPC_METADATA_KEY_ERROR_RESPONSE,
                            JSONUtils.toJSONString(ErrorResponse.builder()
                                    .code(ResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode())
                                    .message(e.getMessage())
                                    .detail(e.getClass().getCanonicalName())
                                    .build()));
                    call.close(Status.fromThrowable(e), trailers);
                }
            }
        };
    }
}
