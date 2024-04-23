package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.service.SkuInfoService;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
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
     * @param skuId 商品的唯一标识符，类型为Long。
     * @return 返回商品详情页面的视图名称。
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId) {
        log.info("准备查询商品详情，商品skuId：{}", skuId);

        SkuItemVo skuItemVo = skuInfoService.item(skuId);

        return "item";
    }
}
