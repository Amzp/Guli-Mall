package com.atguigu.gulimall.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * ClassName: RabbitConfig
 * Package: com.atguigu.gulimall.order.config
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/28 上午9:16
 * @Version 1.0
 */
@Configuration
@Slf4j
public class MyRabbitConfig {

    private RabbitTemplate rabbitTemplate;

    /**
     * 创建并配置RabbitTemplate的Bean实例。
     * 这个方法初始化一个RabbitTemplate实例，并配置它使用给定的ConnectionFactory。
     * 它还调用了一个初始化方法来进一步配置RabbitTemplate。
     *
     * @param connectionFactory 用于创建RabbitTemplate的ConnectionFactory。
     * @return 配置好的RabbitTemplate实例，可用于发送消息到RabbitMQ。
     */
    @Primary
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        this.rabbitTemplate = rabbitTemplate; // 存储创建的RabbitTemplate实例以供其他地方使用
        rabbitTemplate.setMessageConverter(messageConverter()); // 设置消息转换器
        initRabbitTemplate(); // 进一步初始化RabbitTemplate
        return rabbitTemplate;
    }


    /**
     * 初始化并定制RabbitTemplate，以启用RabbitMQ的发布确认和返回确认机制。
     * 该方法配置了RabbitTemplate，以支持消息发送后的确认回调和消息无法路由时的返回回调。
     * 通过启用这些特性，可以提高消息发送的可靠性和故障诊断的效率。
     * <p>
     * 配置要点：
     * 1. 开启发布确认(publisher-confirms)，确保消息成功到达RabbitMQ broker。
     * 2. 设置确认回调，当消息成功抵达队列时触发。
     * 3. 开启返回确认(publisher-returns)，并配置返回回调，用于处理消息无法路由的情况。
     * 4. 配置消息为必须(mandatory)，确保所有无法路由的消息都会被返回。
     * <p>
     * 注意：该方法不接受任何参数，也不返回任何值。
     */
//    @PostConstruct  // MyRabbitConfig对象创建完成以后，执行这个方法
    public void initRabbitTemplate() {
        /**
         * 设置确认回调，用于处理消息到达Broker的确认。
         * 此处配置了RabbitTemplate以支持消息发送后的确认机制，
         * 当消息成功抵达RabbitMQ Broker时，会触发回调函数。
         * 这有助于确保消息的可靠性投递。
         * 注意：回调函数内具体操作根据实际需求编写，此处仅为示例。
         */
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            log.debug("confirm...correlationData [{}] ==> ack: [{}] ==> cause: [{}]", correlationData, ack, cause);
        });


        /**
         * 配置返回回调，用于处理消息无法路由的情况。
         * 当消息无法被正确路由到指定的队列时，会触发这个回调。
         * 这样可以及时发现并处理消息发送的问题。
         * 注意：回调函数内具体操作根据实际需求编写，此处仅为示例。
         */
        rabbitTemplate.setReturnCallback((message,replyCode,replyText,exchange,routingKey) -> {
            log.debug("Fail Message [{}] ==> replyCode [{}] ==> replyText [{}] ==> exchange [{}] ==> routingKey [{}]", message, replyCode, replyText, exchange, routingKey);
        });
    }


    /**
     * 创建并返回一个消息转换器实例。
     * 这个方法不接受任何参数。
     *
     * @return MessageConverter 返回一个Jackson2JsonMessageConverter实例，
     * 用于将消息对象转换为JSON格式，支持Spring消息框架中消息的双向转换。
     */
    @Bean
    public MessageConverter messageConverter() {
        // 创建并返回一个Jackson2JsonMessageConverter实例
        return new Jackson2JsonMessageConverter();
    }


}
