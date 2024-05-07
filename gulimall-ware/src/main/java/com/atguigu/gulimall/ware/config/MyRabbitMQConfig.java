package com.atguigu.gulimall.ware.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class MyRabbitMQConfig {

    /**
     * 创建并配置一个消息转换器，用于将消息对象转换为JSON格式，以及将JSON格式的数据转换回消息对象。
     * 这是通过使用Spring提供的Jackson2JsonMessageConverter实现的。
     *
     * @return Jackson2JsonMessageConverter 实例，用于JSON格式的消息转换。
     */
    @Bean
    public MessageConverter messageConverter() {
        // 返回一个新的Jackson2JsonMessageConverter实例，用于消息的JSON序列化和反序列化
        return new Jackson2JsonMessageConverter();
    }


    // @RabbitListener(queues = "stock.release.queue")
    // public void handle(Message message) {
    //
    // }

    /**
     * 创建并返回一个库存服务默认的交换机。
     * 这个交换机的类型是主题交换机（Topic Exchange），它将持续存在（durable），并且不会自动删除（autoDelete为false）。
     * 主题交换机基于消息的路由键和交换机绑定键的模式来路由消息到一个或多个队列。
     *
     * @return Exchange 返回一个配置好的主题交换机实例。
     */
    @Bean
    public Exchange stockEventExchange() {
        // 创建并返回一个名为"stock-event-exchange"的主题交换机，设置为持久化且不自动删除
        return new TopicExchange("stock-event-exchange", true, false);
    }


    /**
     * 创建并配置一个名为"stock.release.queue"的队列。
     * 这个队列是持久化的，意味着在服务器重启后仍然存在，但它不是排他性的，也不会在最后一个消费者取消订阅后自动删除。
     *
     * @return 返回配置好的Queue对象。
     */
    @Bean
    public Queue stockReleaseStockQueue() {
        // 创建队列对象，配置其持久化、非排他性、不自动删除的属性
        return new Queue("stock.release.queue", true, false, false);
    }



    /**
     * 创建一个延迟队列。该队列具有以下特性：
     * - 使用了死信交换（x-dead-letter-exchange），当消息在队列中超过指定的存活时间（TTL）后，将被发送到名为"stock-event-exchange"的交换器。
     * - 死信路由键（x-dead-letter-routing-key）被设置为"stock.release"，定义了死信应当如何被路由。
     * - 消息存活时间（x-message-ttl）被设置为30分钟（30*60*1000毫秒），任何在队列中等待超过30分钟的消息都将被视为过期并处理。
     *
     * @return 返回配置好的延迟队列对象。
     */
    @Bean
    public Queue stockDelay() {

        // 定义队列的参数，包括死信交换器、路由键和消息存活时间
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        arguments.put("x-dead-letter-routing-key", "stock.release");
        arguments.put("x-message-ttl", 30 * 60 * 1000);

        // 创建并返回一个具有指定参数的队列
        return new Queue("stock.delay.queue", true, false, false, arguments);
    }



    /**
     * 交换机与普通队列绑定
     * 此函数创建一个绑定，将指定的交换机与队列连接起来，以便消息可以被正确路由。
     * @return Binding 返回创建的绑定对象。
     */
    @Bean
    public Binding stockLocked() {
        // 创建一个绑定，将"stock.release.queue"队列与"stock-event-exchange"交换机绑定，
        // 路由键为"stock.release.#"，不使用额外的绑定参数。

        return new Binding("stock.release.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null);
    }



    /**
     * 创建并返回一个绑定对象，将延迟队列与交换机绑定。
     * 这个绑定的作用是确保交换机上的特定类型的消息能够被正确路由到这个延迟队列中。
     *
     * @return Binding 返回一个代表队列和交换机之间绑定关系的对象。
     */
    @Bean
    public Binding stockLockedBinding() {
        // 创建绑定，将"stock.delay.queue"队列与"stock-event-exchange"交换机绑定，
        // 路由键为"stock.locked"，不使用额外的绑定参数。
        return new Binding("stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",
                null);
    }



}
