package com.atguigu.gulimall.member.web;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.feign.OrderFeignService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: Rain^
 * @createTime: 2020-07-08 13:39
 **/

@Controller
public class MemberWebController {

    @Resource
    private OrderFeignService orderFeignService;

    /**
     * 处理会员订单页面的请求。
     *
     * @param pageNum 请求中的页码参数，非必需，默认值为0。
     * @param model 用于在视图和控制器之间传递数据的Model对象。
     * @param request 用户的请求对象，用于获取请求数据。
     * @return 返回订单列表页面的视图名称。
     */
    @GetMapping(value = "/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum",required = false,defaultValue = "0") Integer pageNum,
                                  Model model, HttpServletRequest request) {

        // 从请求中获取支付宝传输的所有数据，用于后续的签名验证

        // 查询当前登录用户的所有订单列表数据
        Map<String,Object> page = new HashMap<>();
        page.put("page",pageNum.toString());

        // 调用远程订单服务，查询订单数据
        R orderInfo = orderFeignService.listWithItem(page);
        System.out.println(JSON.toJSONString(orderInfo));
        // 将查询到的订单数据添加到Model中，供前端页面展示
        model.addAttribute("orders",orderInfo);

        return "orderList"; // 返回订单列表页面
    }


}
