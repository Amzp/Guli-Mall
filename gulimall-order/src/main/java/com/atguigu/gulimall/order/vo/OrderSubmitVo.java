package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;



/**
 * 订单提交相关的VO（值对象）类，用于封装订单提交时所需的信息。
 */
@Data
public class OrderSubmitVo {

    /**
     * 收货地址的id，用于标识用户选择的收货地址。
     **/
    private Long addrId;

    /**
     * 支付方式，表示用户选择的支付方式，例如：1-在线支付，2-货到付款等。
     **/
    private Integer payType;

    // 该注释说明了为何此处不直接提交购物车中的商品信息，以及优惠和发票信息的处理策略。

    /**
     * 防重令牌，用于防止重复提交订单。
     **/
    private String orderToken;

    /**
     * 应付价格，表示用户需要支付的订单总金额。
     **/
    private BigDecimal payPrice;

    /**
     * 订单备注，用户可以对订单添加额外的备注信息。
     **/
    private String remarks;

    // 用户相关的信息将直接从session中获取，而不是通过该VO传递。
}

