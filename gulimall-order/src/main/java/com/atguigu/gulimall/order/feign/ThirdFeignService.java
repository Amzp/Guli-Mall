package com.atguigu.gulimall.order.feign;

import com.alipay.api.AlipayApiException;
import com.atguigu.gulimall.order.vo.PayVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;



@FeignClient("gulimall-third-party")
public interface ThirdFeignService {

    @GetMapping(value = "/pay",consumes = "application/json")
    String pay(@RequestBody PayVo vo) throws AlipayApiException;

}
