package com.atguigu.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.atguigu.common.annotation.LogInfo;
import com.lly835.bestpay.config.WxPayConfig;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.BestPayService;
import com.atguigu.gulimall.order.config.AlipayTemplate;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static com.lly835.bestpay.enums.BestPayTypeEnum.WXPAY_NATIVE;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: Rain^
 * @createTime: 2020-07-08 10:54
 **/

@Slf4j
@Controller
public class PayWebController {

    @Resource
    private AlipayTemplate alipayTemplate;

    @Resource
    private OrderService orderService;

    @Resource
    private BestPayService bestPayService;

    @Resource
    private WxPayConfig wxPayConfig;

    /**
     * 用户下单并使用支付宝进行支付的处理流程。
     * 1. 通过订单号获取支付信息。
     * 2. 调用支付宝接口生成支付页面并让浏览器展示该页面。
     * 3. 用户支付成功后，理论上应跳转回应用内的用户订单列表页，但该逻辑在此示例中未体现。
     *
     * @param orderSn 用户订单号，用于查询对应的支付信息。
     * @return 返回支付宝的支付页面内容，供浏览器展示。
     * @throws AlipayApiException 如果调用支付宝接口过程中出现错误，则抛出异常。
     */
    @ResponseBody
    @GetMapping(value = "/aliPayOrder",produces = "text/html")
    @LogInfo(name = "支付宝支付")
    public String aliPayOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        // 根据订单号获取支付信息
        log.info("发起支付 orderSn={}", orderSn);
        PayVo payVo = orderService.getOrderPay(orderSn);
        // 调用支付宝接口生成支付链接或页面
        log.info("调用支付宝接口，发起支付 payVo={}", payVo);
        String pay = alipayTemplate.pay(payVo);
        return pay;
    }



    /**
     * 微信支付
     * @param orderSn
     * @return
     */
    @GetMapping(value = "/weixinPayOrder")
    @LogInfo(name = "微信支付")
    public String weixinPayOrder(@RequestParam("orderSn") String orderSn, Model model) {

        OrderEntity orderInfo = orderService.getOrderByOrderSn(orderSn);

        if (orderInfo == null) {
            throw new RuntimeException("订单不存在");
        }

        PayRequest request = new PayRequest();
        request.setOrderName("4559066-最好的支付sdk");
        request.setOrderId(orderInfo.getOrderSn());
        request.setOrderAmount(0.01);
        request.setPayTypeEnum(WXPAY_NATIVE);

        PayResponse payResponse = bestPayService.pay(request);
        payResponse.setOrderId(orderInfo.getOrderSn());
        log.info("发起支付 response={}", payResponse);

        //传入前台的二维码路径生成支付二维码
        model.addAttribute("codeUrl",payResponse.getCodeUrl());
        model.addAttribute("orderId",payResponse.getOrderId());
        model.addAttribute("returnUrl",wxPayConfig.getReturnUrl());

        return "createForWxNative";
    }


    //根据订单号查询订单状态的API
    @GetMapping(value = "/queryByOrderId")
    @ResponseBody
    @LogInfo(name = "查询订单")
    public OrderEntity queryByOrderId(@RequestParam("orderId") String orderId) {
        log.info("查询支付记录...");
        return orderService.getOrderByOrderSn(orderId);
    }



}
