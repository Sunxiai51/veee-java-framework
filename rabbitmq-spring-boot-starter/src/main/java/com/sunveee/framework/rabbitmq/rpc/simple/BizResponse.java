package com.sunveee.framework.rabbitmq.rpc.simple;

import java.io.Serializable;


import com.sunveee.framework.rabbitmq.rpc.base.BaseResponse;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class BizResponse<D extends Serializable> extends BaseResponse {

    private D data;

}
