package com.atguigu.gulimall.order.controller;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

/**
 * ClassName: RabbitController
 * Package: com.atguigu.gulimall.order.controller
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/28 上午10:42
 * @Version 1.0
 */
//@RestController
@Slf4j
public class RabbitController {
//    @Resource
    private RabbitTemplate rabbitTemplate;

//    @GetMapping("/sendMq")
    public String sendMq(@RequestParam(defaultValue = "10") Integer num){
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                OrderReturnReasonEntity orderReturnReasonEntity = new OrderReturnReasonEntity();
                orderReturnReasonEntity.setId((long) i);
                orderReturnReasonEntity.setName("test");
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", orderReturnReasonEntity,new CorrelationData(UUID.randomUUID().toString()));
            }else {
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setId((long) i);
                orderEntity.setOrderSn(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", orderEntity,new CorrelationData(UUID.randomUUID().toString()));
            }
        }

        log.info("Message sent successfully!");
        return "Message sent successfully!";
    }

}
