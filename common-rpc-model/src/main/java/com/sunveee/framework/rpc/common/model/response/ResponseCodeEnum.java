package com.sunveee.framework.rpc.common.model.response;

import lombok.Getter;

/**
 * 接口返回码枚举类 ResponseCodeEnum.java
 *
 * @author SunVeee
 * @version 2021-06-18 11:54:14
 */
@Getter
public enum ResponseCodeEnum {
    SUCCESS("200", "成功"),
    QUERY_NOT_FOUND("201", "未查询到数据"),

    BODY_NOT_MATCH("400", "请求的数据格式不符"),

    INTERNAL_SERVER_ERROR("500", "服务器内部错误"),
    LOGIC_EXCEPTION("501", "逻辑异常"),
    DUPLICATE_KEY_EXCEPTION("502", "数据库主键/唯一索引冲突导致异常"),
    CHAIN_INTERACT_EXCEPTION("503", "链交互异常"),

    BIZ_EXCEPTION("700", "业务异常"),
    ILLEGAL_ARGUMENT("701", "参数异常"),
    CONTRACT_INVOKE_ERROR("702", "合约返回异常"),
    AUTH_EXCEPTION("704", "数据越权"),
    ;

    private String code;
    private String message;

    ResponseCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
