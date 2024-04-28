package com.atguigu.gulimall.product.app;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.service.SkuInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;


/**
 * sku信息
 *
 * @author Rain^
 * @email 843524258@qq.com
 * @date 2019-10-01 22:50:32
 */
@RestController
@RequestMapping("product/skuinfo")
@Slf4j
public class SkuInfoController {
    @Resource
    private SkuInfoService skuInfoService;


    /**
     * 获取指定SKU ID的商品价格。
     *
     * @param skuId 商品SKU的唯一标识符。
     * @return 返回对应SKU的价格，类型为BigDecimal。
     */
    @GetMapping("/{skuId}/price")
    public R getPrice(@PathVariable("skuId") Long skuId) {
        log.info("查询商品价格：skuId = {}", skuId);
        // 通过SKU ID获取SKU信息
        SkuInfoEntity skuInfo = skuInfoService.getById(skuId);
        // 返回SKU的价格
        return R.ok().setData(skuInfo.getPrice().toString());
    }

    /**
     * 查询SKU信息列表
     *
     * @param params 包含查询条件的参数映射
     * @return 返回包含查询结果的页面信息
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:skuinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        log.info("查询SKU信息列表...");
        // 根据条件查询SKU信息页面
        PageUtils page = skuInfoService.queryPageByCondition(params);

        // 返回查询结果
        return R.ok().put("page", page);
    }



    /**
     * 获取指定SKU的信息
     *
     * @param skuId SKU的唯一标识符
     * @return 返回一个包含SKU信息的响应对象
     */
    @RequestMapping("/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId){
        log.info("获取指定SKU的信息：skuId = {}", skuId);
        // 通过SKU ID从服务中获取SKU信息
        SkuInfoEntity skuInfo = skuInfoService.getById(skuId);

        // 构建并返回一个包含SKU信息的成功响应
        return R.ok().put("skuInfo", skuInfo);
    }


    /**
     * 保存SKU信息
     *
     * @param skuInfo SKU信息实体，通过RequestBody接收前端传来的JSON数据
     * @return 返回操作结果，成功则返回一个OK标识
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:skuinfo:save")
    public R save(@RequestBody SkuInfoEntity skuInfo){
        log.info("保存SKU信息：SkuId = {}", skuInfo.getSkuId());
        // 调用SKU信息服务层方法，保存SKU信息
        skuInfoService.save(skuInfo);

        // 返回操作成功的标识
        return R.ok();
    }


    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:skuinfo:update")
    public R update(@RequestBody SkuInfoEntity skuInfo){
        log.info("修改SKU信息：SkuId = {}", skuInfo.getSkuId());
		skuInfoService.updateById(skuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:skuinfo:delete")
    public R delete(@RequestBody Long[] skuIds){
        log.info("删除SKU信息：SkuIds = {}", Arrays.toString(skuIds));
		skuInfoService.removeByIds(Arrays.asList(skuIds));

        return R.ok();
    }

}
