package com.atguigu.gulimall.auth.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ClassName: ThirdPartFeignService
 * Package: com.atguigu.gulimall.auth.feign
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/24 下午5:03
 * @Version 1.0
 */
@FeignClient("gulimall-third-party")
public interface ThirdPartFeignService {

    @GetMapping(value = "/sms/sendCode")
    R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
