package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    /**
     * 保存SPU积分规则信息
     * <p> 1、将传入的SpuBoundTo对象转换为JSON格式。
     * <p> 2、向gulimall-coupon服务的/coupon/spubounds/save路径发送POST请求，将转换后的JSON数据放在请求体中。
     * <p> 3、接收方服务收到请求，将请求体中的JSON数据转换为SpuBoundsEntity实体。
     * <p> 只要JSON数据模型兼容，调用方和服务提供方可以使用不同的对象进行转换。
     *
     * @param spuBoundTo 积分规则信息对象
     * @return 返回操作结果，封装在R对象中
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    /**
     * 保存SKU满减信息
     * <p> 1、将传入的SkuReductionTo对象转换为JSON格式。
     * <p> 2、向gulimall-coupon服务的/coupon/skufullreduction/saveinfo路径发送POST请求，将转换后的JSON数据放在请求体中。
     * <p> 3、接收方服务收到请求，将请求体中的JSON数据转换为SkuReductionTo实体。
     *
     * @param skuReductionTo SKU满减信息对象
     * @return 返回操作结果，封装在R对象中
     */
    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
