package com.atguigu.common.constant;


/**
 * 购物车常量类，用于定义购物车相关的常量。
 */
public class CartConstant {

    // 用于临时用户的身份验证的cookie名称
    public final static String TEMP_USER_COOKIE_NAME = "user-key";

    // 临时用户cookie的超时时间，单位为秒，这里设置为30天
    public final static int TEMP_USER_COOKIE_TIMEOUT = 60 * 60 * 24 * 30;

    // Redis中购物车数据存储的前缀
    public final static String CART_PREFIX = "gulimall:cart:";

}
