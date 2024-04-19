package com.atguigu.common.exception;

/**
 * 错误码和错误信息定义类
 * <p> 1. 错误码定义规则为5位数字，其中:
 * <p>    - 前两位表示业务场景，最后三位表示错误码。
 * <p>    - 例如：100001，其中10表示通用场景，001表示系统未知异常。
 * <p> 2. 该类维护了所有错误码及其对应的错误描述，以枚举形式进行定义。
 * <p> 3. 错误码列表包括但不限于以下场景：
 * <p>    - 10: 通用
 * <p>        - 001：参数格式校验
 * <p>    - 11: 商品
 * <p>    - 12: 订单
 * <p>    - 13: 购物车
 * <p>    - 14: 物流
 */
public enum BizCodeEnume {
    // 系统未知异常，错误码为10000，错误信息为"系统未知异常"
    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    // 参数格式校验失败，错误码为10001，错误信息为"参数格式校验失败"
    VAILD_EXCEPTION(10001, "参数格式校验失败"),
    // 商品上架异常，错误码为11000，错误信息为"商品上架异常"
    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),

    // 购物车服务异常，错误码为12000，错误信息为"购物车服务异常"
    CART_SERVICE_EXCEPTION(12000, "购物车服务异常"),
    // 订单服务异常，错误码为13000，错误信息为"订单服务异常"
    ORDER_SERVICE_EXCEPTION(13000, "订单服务异常"),
    // 订单服务异常，错误码为14000，错误信息为"物流服务异常"
    WARE_SERVICE_EXCEPTION(14000, "物流服务异常"),
    // 订单服务异常，错误码为15000，错误信息为"秒杀服务异常"
    SECKILL_SERVICE_EXCEPTION(15000, "秒杀服务异常");


    // 错误码
    private int code;
    // 错误信息
    private String msg;

    /**
     * 构造函数，用于初始化错误码和错误信息。
     *
     * @param code 错误码
     * @param msg  错误信息
     */
    BizCodeEnume(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 获取错误码。
     *
     * @return 错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取错误信息。
     *
     * @return 错误信息
     */
    public String getMsg() {
        return msg;
    }
}
