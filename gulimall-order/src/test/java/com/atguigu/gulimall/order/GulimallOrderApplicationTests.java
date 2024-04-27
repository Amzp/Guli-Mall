package com.atguigu.gulimall.order;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class GulimallOrderApplicationTests {
    @Resource
    private AmqpAdmin amqpAdmin;

    @Test
    public void testCreatExchange() {
        long startTime = System.currentTimeMillis();
        System.out.println("testCreatExchange()\n");


        // testCreatExchange Code
        // 创建一个DirectExchange实例
        // 参数1: "hello-java-exchange"，交换机的名称，用于标识这个交换机
        // 参数2: true，表示这个交换机是持久化的，即在RabbitMQ服务重启后仍然存在
        // 参数3: false，表示这个交换机不具有自动删除属性，即只有当所有绑定这个交换机的队列都被删除后，它才会被删除
        DirectExchange directExchange = new DirectExchange(
                "hello-java-exchange",
                true,
                false);

        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange {} created successfully!", directExchange.getName());


        System.out.printf("\ntestCreatExchange  Execution time: %d ms", (System.currentTimeMillis() - startTime));
    }

    @Test
    public void testCreateQueue() {
        long startTime = System.currentTimeMillis();
        System.out.println("testCreateQueue()\n");


        // testCreateQueue Code
        /**
         * 创建一个新的队列实例。
         * @param name 队列的名称，用于标识这个队列。
         * @param isDurable 指示队列是否应该在服务器重启后仍然存在。true表示持久化，false表示非持久化。
         * @param isExclusive 指示队列是否应该是排他性的。true表示这个队列只对一个连接可见，false表示对所有连接可见。
         * @param isAutoDelete 指示队列是否应该在最后一个消费者取消订阅后自动删除。true表示自动删除，false表示不自动删除。
         * @return 队列实例的引用。
         */
        Queue queue = new Queue(
                "hello-java-queue",
                true,
                false,
                false);

        amqpAdmin.declareQueue(queue);
        log.info("Queue {} created successfully!", queue.getName());


        System.out.printf("\ntestCreateQueue  Execution time: %d ms", (System.currentTimeMillis() - startTime));
    }


    @Test
    public void testCreateBinding() {
        long startTime = System.currentTimeMillis();
        System.out.println("testCreateBinding()\n");


        // testCreateBinding Code
        // 声明一个绑定
        amqpAdmin.declareBinding(
                new org.springframework.amqp.core.Binding(
                        "hello-java-queue", // 目标队列名称
                        org.springframework.amqp.core.Binding.DestinationType.QUEUE, // 目标类型为队列
                        "hello-java-exchange", // 源交换器名称
                        "hello.java", // 绑定键
                        null)); // 无额外属性
        log.info("Binding created successfully!");



        System.out.printf("\ntestCreateBinding  Execution time: %d ms", (System.currentTimeMillis() - startTime));
    }

}
