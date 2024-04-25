package com.atguigu.gulimall.coupon.controller;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.atguigu.gulimall.coupon.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * 优惠券信息
 *
 * @author Rain^
 * @email 843524258@qq.com
 * @date 2019-10-08 09:36:40
 */
/*  @RefreshScope 是 Spring Cloud 中的一个重要注解，用于在运行时刷新配置属性。
    当使用配置中心（如 Nacos、Consul、Spring Cloud Config Server 等）维护外部配置时，@RefreshScope 可以使得更新配置不需要重启应用。
    标记有 @RefreshScope 的 Bean 在配置更改时将会被刷新。最常见的用途是在配置客户端的场景中，用于动态更新配置属性。
    使用方式：
        @RefreshScope通常用于声明一个Bean应当在环境配置更改时刷新。
        将这个注解添加到@Component或@Configuration注解的类上，可以使得这些类中的属性值在配置更新后自动刷新。*/
@RefreshScope   // 使得配置能够实时刷新
@RestController
@RequestMapping("coupon/coupon")
public class CouponController {
    @Autowired
    private CouponService couponService;

    // @Value注解用于将外部配置的值注入到Spring管理的Bean中
    @Value("${coupon.user.name}")
    private String name;
    @Value("${coupon.user.age}")
    private Integer age;

    @RequestMapping("/test")
    public R test() {
        return R.ok()
                .put("name", name)
                .put("age", age);
    }

    @RequestMapping("/member/list")
    public R membercoupons() {
        CouponEntity couponEntity = new CouponEntity();
        couponEntity.setCouponName("满100减10");
        return R.ok()
                .put("coupons", Arrays.asList(couponEntity));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("coupon:coupon:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = couponService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("coupon:coupon:info")
    public R info(@PathVariable("id") Long id) {
        CouponEntity coupon = couponService.getById(id);

        return R.ok().put("coupon", coupon);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("coupon:coupon:save")
    public R save(@RequestBody CouponEntity coupon) {
        couponService.save(coupon);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("coupon:coupon:update")
    public R update(@RequestBody CouponEntity coupon) {
        couponService.updateById(coupon);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("coupon:coupon:delete")
    public R delete(@RequestBody Long[] ids) {
        couponService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
