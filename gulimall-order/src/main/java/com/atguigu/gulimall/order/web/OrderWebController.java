package com.atguigu.gulimall.order.web;

import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.annotation.LogInfo;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
//@Api(tags = "订单模块")
public class OrderWebController {

    @Resource
    private OrderService orderService;


    @GetMapping("/toTrade")
    @LogInfo(name = "订单确认页")
//    @ApiOperation(value = "订单确认页")
    public String toTrade(Model model) {

        OrderConfirmVo confirmVo = orderService.confirmOrder();

        model.addAttribute("confirmOrderData", confirmVo);
        log.info("跳转到订单确认页");
        return "confirm";
    }


    /**
     * 处理提交订单的HTTP POST请求。
     * 此方法接收从客户端提交的订单信息，通过调用订单服务进行订单提交操作。
     * 它会根据订单提交的结果来决定下一步的页面跳转逻辑：成功则转向支付页面，
     * 失败则根据具体错误信息重定向回订单确认页面并显示错误提示。
     *
     * @param vo         OrderSubmitVo 类型的参数，封装了用户提交的订单详情信息，
     *                   包括但不限于商品列表、收货地址、支付方式、优惠信息等。
     * @param model      Model 类型，用于向视图传递数据。这里用于在成功提交订单后，
     *                   向“pay”页面传递订单响应数据。
     * @param attributes RedirectAttributes 类型，用于在重定向时携带临时性的闪存属性。
     *                   在本方法中用于携带错误消息到订单确认页面。
     * @return 返回一个字符串，指示下一步的页面跳转路径。
     * - 若订单提交成功，返回 "pay" 表示跳转到支付页面。
     * - 若订单提交失败或遇到异常，返回一个重定向URL至订单确认页面，
     * 并通过Flash属性携带错误信息。
     */
    @PostMapping("/submitOrder")
    @LogInfo(name = "提交订单")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes attributes) {
        log.debug("提交订单：{}", vo.toString());

        try {
            // 调用订单服务的submitOrder方法，传入订单提交Vo对象，获取订单提交响应Vo对象。
            SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);

            // 判断订单提交是否成功
            if (responseVo.getCode() == 0) {
                // 订单提交成功逻辑
                // 将订单响应数据添加到Model中，以便在"pay"页面展示。
                model.addAttribute("submitOrderResp", responseVo);
                log.info("订单提交成功，订单号：{}", responseVo.getOrder().getOrderSn());
                return "pay"; // 跳转至支付页面
            } else {
                // 订单提交失败逻辑
                // 根据错误代码构建错误信息，并使用重定向属性携带错误信息回到订单确认页面。
                String msg = "下单失败";
                switch (responseVo.getCode()) {
                    case 1:
                        msg += "：订单信息过期，请刷新再次提交！";
                        break;
                    case 2:
                        msg += "：订单商品价格发生变化，请确认后再次提交！";
                        break;
                    case 3:
                        msg += "：库存锁定失败，商品库存不足！";
                        break;
                }
                attributes.addFlashAttribute("msg", msg);
                log.info("订单提交失败，错误代码：{}", responseVo.getCode());
                return "redirect:http://order.gulimall.com/toTrade";     // 重定向至订单确认页面
            }
        } catch (Exception e) {
            // 特别处理库存不足异常，其他异常直接重定向回订单确认页面。
            if (e instanceof NoStockException) {
                String message = e.getMessage();
                log.info("订单提交失败，库存不足：{}", message);
                attributes.addFlashAttribute("msg", message);
            }
            log.warn("订单提交失败，异常信息：{}", e.getMessage());
            return "redirect:http://order.gulimall.com/toTrade";    // 重定向至订单确认页面
        }
    }


}
