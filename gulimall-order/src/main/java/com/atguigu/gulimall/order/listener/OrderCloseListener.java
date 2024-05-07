package com.atguigu.gulimall.order.listener;

import com.atguigu.common.annotation.LogInfo;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;


@RabbitListener(queues = "order.release.queue")
@Service
@Slf4j
public class OrderCloseListener {

    @Resource
    private OrderService orderService;

    /**
     * 处理接收到的订单信息，并根据处理结果进行消息的确认或拒绝。
     *
     * @param orderEntity 接收到的订单实体，包含订单的各种信息。
     * @param channel AMQP消息通道，用于确认消息处理结果。
     * @param message 接收到的消息对象，包含消息的各种属性。
     * @throws IOException 在与消息队列交互过程中可能抛出的异常。
     */
    @RabbitHandler
    @LogInfo(name = "订单关闭")
    public void listener(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
        // 打印订单关闭信息
        log.info("收到过期的订单信息，准备关闭订单[{}]", orderEntity.getOrderSn());
        try {
            // 尝试关闭订单
            orderService.closeOrder(orderEntity);
            // 如果关闭订单成功，则确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            // 如果关闭订单失败，则拒绝消息
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }

}
