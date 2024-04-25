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
 * <p>        - 002：验证码已发送，请稍后再试
 * <p>    - 11: 商品
 * <p>    - 12: 订单
 * <p>    - 13: 购物车
 * <p>    - 14: 物流
 * <p>    - 15: 用户
 */
public enum BizCodeEnume {
    // 定义系统中可能出现的各种异常类型，包括异常编码和异常信息
    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    // 表示参数格式校验失败的异常
    VAILD_EXCEPTION(10001, "参数格式校验失败"),
    // 表示在短时间内频繁获取验证码导致的异常
    SMS_CODE_EXCEPTION(10002, "验证码已发送，请稍后再试"),
    // 表示商品上架过程中出现的异常
    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),

    // 用户注册时，发现已存在相同用户名的异常
    USER_EXIST_EXCEPTION(15001,"存在相同的用户"),
    // 用户注册时，发现已存在相同手机号的异常
    PHONE_EXIST_EXCEPTION(15002,"存在相同的手机号"),
    // 下单时，发现商品库存不足的异常
    NO_STOCK_EXCEPTION(21000,"商品库存不足"),
    // 用户登录时，账号或密码错误的异常
    LOGINACCT_PASSWORD_EXCEPTION(15003,"账号或密码错误"),
    ;



    // 错误码
    private Integer code;
    // 错误信息
    private String msg;

    /**
     * 构造函数，用于初始化错误码和错误信息。
     *
     * @param code 错误码
     * @param msg  错误信息
     */
    BizCodeEnume(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 获取错误码。
     *
     * @return 错误码
     */
    public Integer getCode() {
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
