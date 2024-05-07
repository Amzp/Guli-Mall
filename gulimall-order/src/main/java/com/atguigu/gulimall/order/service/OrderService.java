package com.atguigu.gulimall.order.service;

import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 订单
 *
 * @author Rain^
 * @email 843524258@qq.com
 * @date 2019-10-08 09:56:16
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder();

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    OrderEntity getOrderByOrderSn(String orderSn);

    void closeOrder(OrderEntity orderEntity);

    PayVo getOrderPay(String orderSn);

    String handlePayResult(PayAsyncVo asyncVo);

    String asyncNotify(String notifyData);

    void createSeckillOrder(SeckillOrderTo orderTo);

    PageUtils queryPageWithItem(Map<String, Object> params);
}

