package com.atguigu.gulimall.ware.listener;

import com.rabbitmq.client.Channel;
import com.atguigu.common.to.OrderTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.gulimall.ware.service.WareSkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: Rain^
 * @createTime: 2020-07-07 00:20
 **/

@Slf4j
@RabbitListener(queues = "stock.release.queue")
@Service
public class StockReleaseListener {

    @Resource
    private WareSkuService wareSkuService;

    /**
     * 1、库存自动解锁
     *  下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。之前锁定的库存就要自动解锁
     *
     *  2、订单失败
     *      库存锁定失败
     *
     *   只要解锁库存的消息失败，一定要告诉服务解锁失败
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        log.info("******收到解锁库存的信息******");
        try {

            //当前消息是否被第二次及以后（重新）派发过来了
            // Boolean redelivered = message.getMessageProperties().getRedelivered();

            //解锁库存
            wareSkuService.unlockStock(to);
            // 手动删除消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            // 解锁失败 将消息重新放回队列，让别人消费
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    /**
     * 处理订单关闭释放库存的逻辑。
     * 当收到订单关闭的消息时，尝试解锁对应订单的库存。
     * 如果解锁成功，则手动删除消息；如果解锁失败，则将消息重新放回队列。
     *
     * @param orderTo 包含订单信息的对象
     * @param message RabbitMQ的消息对象
     * @param channel RabbitMQ的通道对象，用于确认消息处理结果
     * @throws IOException 在处理消息或确认消息时可能抛出的异常
     */
    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo orderTo, Message message, Channel channel) throws IOException {

        log.info("******收到订单关闭，准备解锁库存的信息******");

        try {
            wareSkuService.unlockStock(orderTo); // 尝试根据订单信息解锁库存
            // 手动删除消息，确认消息已被成功处理
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            // 解锁失败，将消息重新放回队列，让其他消费者尝试处理
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }



}
