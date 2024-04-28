package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;

/**
 * ClassName: OrderWebController
 * Package: com.atguigu.gulimall.order.web
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/28 下午4:01
 * @Version 1.0
 */
@Controller
@Slf4j
public class OrderWebController {

    @Resource
    private OrderService orderService;



    @GetMapping("/toTrade")
    public String toTrade(Model model) {

        OrderConfirmVo confirmVo = orderService.confirmOrder();

        model.addAttribute("confirmOrderData", confirmVo);
        log.info("跳转到订单确认页");
        return "confirm";
    }



}
