package com.sunveee.framework.rabbitmq.rpc.util;

public class ResponseCodeConstant {

    /*
     * 【0xxxxx】成功
     * 
     * 服务端业务执行完成后正常返回
     */
    public static final String SUCCESS = "000000";

    /*
     * 【1xxxxx】入参异常
     * 
     * 客户端入参没有通过服务端的校验时返回，例如缺少必传参数、参数格式不符合要求等
     * 
     * 客户端建议告警级别：WARN or ERROR
     */
    public static final String PARAM_ILLEGAL = "100000";

    /*
     * 【2xxxxx】业务失败
     * 
     * 因业务原因导致服务端返回了非常规结果，例如扣减账户余额时账户已冻结导致余额扣减失败
     * 
     * 客户端建议告警级别：INFO or WARN
     */
    public static final String BIZ_FAILED = "200000";

    /*
     * 【9xxxxx】系统异常
     * 
     * 由于系统原因导致的异常，与业务场景无关，例如SQL执行异常，外部接口超时等
     * 
     * 客户端建议告警级别：ERROR
     */
    public static final String SYSTEM_ERROR = "900000";
    public static final String UNKNOWN_ERROR = "999999";

}
