package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.service.SkuInfoService;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;

/**
 * ClassName: ItemController
 * Package: com.atguigu.gulimall.product.web
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/23 上午10:31
 * @Version 1.0
 */
@Controller
@Slf4j
public class ItemController {

    @Resource
    private SkuInfoService skuInfoService;

    /**
     * 处理商品详情页面的请求。
     *
     * @param skuId 商品的唯一标识符，类型为Long。这个参数通过URL路径变量传递。
     * @param model Model对象用于在视图和控制器之间传递数据。
     * @return 返回商品详情页面的视图名称。在这个例子中，返回的是"item"。
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) {
        // 记录日志：开始查询指定SKU ID的商品详情
        log.info("准备查询商品详情，商品skuId：{}", skuId);

        // 调用服务层方法，查询商品详情，并将结果封装到SkuItemVo对象中
        SkuItemVo skuItemVo = skuInfoService.item(skuId);

        // 将商品详情对象添加到Model中，以便在视图中使用
        model.addAttribute("item", skuItemVo);

        // 返回视图名称
        return "item";
    }

}
