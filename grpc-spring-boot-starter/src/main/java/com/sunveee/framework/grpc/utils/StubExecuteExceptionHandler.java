package com.sunveee.framework.grpc.utils;

import com.sunveee.framework.common.exceptions.exception.BizException;
import com.sunveee.framework.common.exceptions.exception.LogicException;
import com.sunveee.framework.common.utils.json.JSONUtils;
import com.sunveee.framework.grpc.common.GrpcStarterConstants;
import com.sunveee.framework.rpc.common.model.exception.ErrorResponse;
import com.sunveee.framework.rpc.common.model.response.ResponseCodeEnum;
import io.grpc.Metadata;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * ClientExceptionHandler
 *
 * @author SunVeee
 * @date 2022/3/26 00:17
 */
@Slf4j
public class StubExecuteExceptionHandler {

    /**
     * 处理可识别的异常
     * <p>
     * 该方法尝试识别grpc stub执行过程中抛出的异常，如该异常包含服务端返回的可识别信息，将其包装成可识别的异常抛出，否则不作处理
     *
     * @param throwable
     */
    public static void handleRecognizableThrowable(Throwable throwable) {
        Status status = Status.fromThrowable(throwable);
        Metadata trailers = Status.trailersFromThrowable(throwable);
        if (null != status && null != trailers
                && trailers.containsKey(GrpcStarterConstants.GRPC_METADATA_KEY_ERROR_RESPONSE)) {
            ErrorResponse errorResponse = JSONUtils.parseObject(trailers.get(GrpcStarterConstants.GRPC_METADATA_KEY_ERROR_RESPONSE), ErrorResponse.class);
            // BizException
            if (StringUtils.equals(errorResponse.getCode(), ResponseCodeEnum.BIZ_EXCEPTION.getCode())) {
                // TODO 因trailers中的中文乱码问题未解决，这里暂时采用status.getDescription()作为message
                throw new BizException(status.getDescription(), errorResponse.getDetail());
            }
            // LogicException
            else if (StringUtils.equals(errorResponse.getCode(), ResponseCodeEnum.LOGIC_EXCEPTION.getCode())) {
                // TODO 因trailers中的中文乱码问题未解决，这里暂时采用status.getDescription()作为message
                throw new LogicException(status.getDescription(), errorResponse.getDetail());
            }
            // other: LogicException
            else {
                log.warn("Unknown errorResponse: {}", errorResponse);
                // TODO 因trailers中的中文乱码问题未解决，这里暂时采用status.getDescription()作为message
                throw new LogicException(status.getDescription(), errorResponse.getDetail());
            }
        }
    }
}
