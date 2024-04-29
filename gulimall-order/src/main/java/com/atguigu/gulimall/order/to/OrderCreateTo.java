package com.atguigu.gulimall.order.to;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;



/**
 * 订单创建转换器类，用于组装订单创建相关的数据。
 */
@Data
public class OrderCreateTo {

    private OrderEntity order; // 订单实体

    private List<OrderItemEntity> orderItems; // 订单项实体列表

    /**
     * 订单计算的应付价格，包括商品价格、优惠等后的实际支付金额。
     */
    private BigDecimal payPrice;

    /**
     * 运费，订单配送所产生的费用。
     */
    private BigDecimal fare;
}

