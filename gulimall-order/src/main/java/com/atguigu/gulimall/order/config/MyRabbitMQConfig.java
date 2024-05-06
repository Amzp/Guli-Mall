package com.atguigu.gulimall.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: RabbitMQConfig
 * Package: com.atguigu.gulimall.order.config
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/30 下午1:47
 * @Version 1.0
 */
@Configuration
@Slf4j
public class MyRabbitMQConfig {

    /* 容器中的Queue、Exchange、Binding 会自动创建（在RabbitMQ）不存在的情况下 */

    /**
     * 创建一个名为order.delay.queue的队列，这是一个具有死信功能的队列。
     * 此队列配置了以下特性：
     * - 死信交换器（x-dead-letter-exchange）为order-event-exchange，当消息在队列中超过指定的存活时间（TTL）或无法被正确消费时，这些消息将被发送到这个死信交换器。
     * - 死信路由键（x-dead-letter-routing-key）为order.release.order，指定了死信被发送到死信交换器时使用的路由键。
     * - 消息存活时间（x-message-ttl）为60000毫秒，即60秒。任何在队列中等待超过60秒的消息都将被视为超时并被发送到死信交换器。
     *
     * @return 返回配置好的Queue对象，代表了一个具有特定配置的队列。
     */
    @Bean
    public Queue orderDelayQueue() {

        // 定义队列的参数，配置死信交换器、路由键和消息存活时间
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        arguments.put("x-message-ttl", 60000);

        // 创建并返回一个队列实例，该队列为持久化队列，不允许被其他虚拟主机访问，不自动删除
        return new Queue("order.delay.queue", true, false, false, arguments);
    }


    /**
     * 创建并配置订单释放队列的Bean。
     *
     * @return Queue 返回一个配置好的队列实例。
     */
    @Bean
    public Queue orderReleaseQueue() {
        // 创建一个持久化、非排他、不自动删除的队列
        return new Queue("order.release.queue", true, false, false);
    }


    /**
     * 创建并配置一个名为 "order-event-exchange" 的主题类型交换机。
     * 该交换机是持久化的，不支持分区，并且配置为非自动删除模式。
     *
     * @return Exchange 返回配置好的交换机实例。
     */
    @Bean
    public Exchange orderEventExchange() {
        // 创建并返回一个主题类型的交换机实例
        return new TopicExchange("order-event-exchange", true, false);
    }


    /**
     * 创建一个绑定 Bean，用于将消息绑定到订单创建队列。
     *
     * @return Binding 返回一个订单创建队列的绑定实例。
     */
    @Bean
    public Binding orderCreateBinding() {
        // 创建一个绑定，将"order.delay.queue"队列绑定到名为"order-event-exchange"的交换器，
        // 使用"order.create.order"作为路由键。
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.create.order", null);

    }



    /**
     * 创建并返回一个Binding对象，用于将消息队列"order.release.queue"与交换器"order-event-exchange"绑定，
     * 并指定绑定的键为"order.release.order"。这个绑定不包含任何额外的绑定参数。
     *
     * @return Binding 返回一个Binding对象，用于消息队列和交换器之间的绑定。
     */
    @Bean
    public Binding orderReleaseBinding() {
        // 创建绑定，指定源队列、目标类型、交换器名称、绑定键和无额外参数
        return new Binding("order.release.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release.order", null);
    }

    /**
     * 订单释放直接和库存释放进行绑定
     * @return
     */
    @Bean
    public Binding orderReleaseOtherBinding() {

        return new Binding("stock.release.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#",
                null);
    }


    /**
     * 商品秒杀队列
     * @return
     */
    @Bean
    public Queue orderSecKillOrrderQueue() {
        return new Queue("order.seckill.queue", true, false, false);
    }

    @Bean
    public Binding orderSecKillOrrderQueueBinding() {
        //String destination, DestinationType destinationType, String exchange, String routingKey,
        // 			Map<String, Object> arguments

        return new Binding(
                "order.seckill.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.seckill.order",
                null);
    }


}
