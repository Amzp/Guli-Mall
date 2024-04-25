package com.atguigu.gulimall.coupon.controller;

import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.coupon.entity.SkuFullReductionEntity;
import com.atguigu.gulimall.coupon.service.SkuFullReductionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;


/**
 * 商品满减信息
 *
 * @author Rain^
 * @email 843524258@qq.com
 * @date 2019-10-08 09:36:40
 */
@RestController
@RequestMapping("coupon/skufullreduction")
@Slf4j
public class SkuFullReductionController {
    @Resource
    private SkuFullReductionService skuFullReductionService;


    /**
     * 保存商品满减信息
     * @param reductionTo 包含商品满减信息的对象
     * @return 返回操作结果，成功返回ok
     */
    @PostMapping("/saveinfo")
    public R saveInfo(@RequestBody SkuReductionTo reductionTo){
        log.info("保存商品满减信息：{}", reductionTo);
        // 调用服务保存商品满减信息
        skuFullReductionService.saveSkuReduction(reductionTo);
        return R.ok();
    }


    /**
     * 查询优惠券满减信息列表
     *
     * @param params 查询参数，包括分页和过滤条件等
     * @return 返回查询结果的包装对象，包括分页信息和查询到的数据列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("coupon:skufullreduction:list")
    public R list(@RequestParam Map<String, Object> params){
        log.info("查询优惠券满减信息列表：{}", params);
        // 调用服务查询指定条件下的页面数据
        PageUtils page = skuFullReductionService.queryPage(params);

        // 将查询结果包装成成功响应并返回
        return R.ok().put("page", page);
    }


    /**
     * 获取指定ID的优惠券满减信息
     *
     * @param id 优惠券满减的ID
     * @return 返回一个包含优惠券满减信息的响应对象
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("coupon:skufullreduction:info")
    public R info(@PathVariable("id") Long id){
        log.info("获取指定ID的优惠券满减信息：{}", id);
        // 通过ID从服务层获取优惠券满减实体
        SkuFullReductionEntity skuFullReduction = skuFullReductionService.getById(id);

        // 将获取到的优惠券满减信息放入响应对象中并返回
        return R.ok().put("skuFullReduction", skuFullReduction);
    }


    /**
     * 保存SKU满减信息
     *
     * @param skuFullReduction 包含SKU满减详细信息的实体对象
     * @return 返回操作结果，成功则返回一个包含成功标识的R对象
     */
    @RequestMapping("/save")
    //@RequiresPermissions("coupon:skufullreduction:save")
    public R save(@RequestBody SkuFullReductionEntity skuFullReduction){
        log.info("保存SKU满减信息：{}", skuFullReduction);
        // 调用服务层方法，保存SKU满减信息
        skuFullReductionService.save(skuFullReduction);

        // 返回操作成功的响应
        return R.ok();
    }


    /**
     * 修改SKU满减信息
     *
     * @param skuFullReduction 包含要更新的SKU满减信息的实体
     * @return 返回操作结果，成功返回OK
     */
    @RequestMapping("/update")
    //@RequiresPermissions("coupon:skufullreduction:update")
    public R update(@RequestBody SkuFullReductionEntity skuFullReduction){
        log.info("修改SKU满减信息：{}", skuFullReduction);
        // 通过ID更新SKU满减信息
        skuFullReductionService.updateById(skuFullReduction);

        return R.ok(); // 返回操作成功的标志
    }


    /**
     * 删除指定ID的优惠信息
     *
     * @param ids 优惠信息的ID数组，通过RequestBody接收
     * @return 返回操作结果，成功返回OK
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("coupon:skufullreduction:delete")
    public R delete(@RequestBody Long[] ids){
        log.info("删除指定ID的优惠信息：{}", Arrays.toString(ids));
        // 批量删除指定ID的优惠信息
        skuFullReductionService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }


}
