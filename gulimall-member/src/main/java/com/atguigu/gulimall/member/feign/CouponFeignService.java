package com.atguigu.gulimall.member.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 这是一个声明式的远程调用
 *
 * <p>@ FeignClient("gulimall-coupon") 是Spring Cloud中用于声明一个Feign客户端的注解。
 *          在该注解中，参数"gulimall-coupon"代表了要访问的目标服务的名称，Feign客户端会根据该名称去注册中心中查找对应的服务进行调用。
 *          Feign是一个声明式的、模板化的HTTP客户端，通过Feign可以更加方便地调用RESTful服务。
 *          使用@FeignClient注解可以将一个接口标记为一个Feign客户端，并指定要调用的服务名称。
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    /**
     * 查询会员优惠券信息
     * 无参数
     *
     * @return 返回优惠券信息的响应对象
     */
    @RequestMapping("/coupon/coupon/member/list")
    public R membercoupons();

}
