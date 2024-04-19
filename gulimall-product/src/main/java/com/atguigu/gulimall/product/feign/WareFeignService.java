package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * ClassName: WareFeignService
 * Package: com.atguigu.gulimall.product.feign
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/18 下午11:43
 * @Version 1.0
 */
@FeignClient("guilimall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hasstock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);

}
