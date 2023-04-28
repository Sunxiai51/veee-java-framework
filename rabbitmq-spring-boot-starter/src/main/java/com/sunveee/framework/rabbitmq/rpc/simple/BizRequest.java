package com.sunveee.framework.rabbitmq.rpc.simple;

import java.io.Serializable;

import com.sunveee.framework.rabbitmq.rpc.base.BaseRequest;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class BizRequest<P extends Serializable> extends BaseRequest {

    private P param;

}
