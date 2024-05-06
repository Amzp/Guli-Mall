package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;



@Controller
public class HelloController {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 创建订单测试
     *
     * @return 返回字符串 "ok"，表示订单创建测试完成
     */
    @ResponseBody
    @GetMapping(value = "/test/createOrder")
    public String createOrderTest() {

        // 初始化订单实体
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString()); // 设置订单编号
        orderEntity.setModifyTime(new Date()); // 设置修改时间

        // 向MQ发送订单创建消息
        rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",orderEntity);

        return "ok";
    }

    @GetMapping(value = "/{page}.html")
    public String listPage(@PathVariable("page") String page) {

        return page;
    }



}
