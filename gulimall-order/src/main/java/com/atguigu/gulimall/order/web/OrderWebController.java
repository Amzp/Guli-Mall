package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

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

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo) {
        log.info("提交订单：{}", vo.toString());

        SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);

        if (responseVo.getCode() == 0) {
            //下单成功来到支付选择页
            log.debug("下单成功");
            return "pay";
        } else {
            log.debug("下单失败");
            return "redirect:http://order.gulimall.com/toTrade";
        }

    }


}
