package com.atguigu.gulimall.ware.controller;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.SkuHasStockVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 商品库存
 *
 * @author Rain^
 * @email 843524258@qq.com
 * @date 2019-10-08 09:59:40
 */
@RestController
@RequestMapping("ware/waresku")
@Slf4j
public class WareSkuController {
    @Resource
    private WareSkuService wareSkuService;

    /**
     * 锁定库存
     * 该方法用于在特定场景下锁定库存，具体场景包括：
     * 1）、下订单成功后，无论订单是因未支付而被系统自动取消，还是被用户手动取消，都需要解锁库存。
     * 2）、下订单成功且库存锁定成功后，如果后续业务调用失败导致订单回滚，之前锁定的库存需自动解锁。
     * @param vo 库存锁定场景的详细信息，包括需要锁定的商品信息和数量等。
     * @return 返回一个结果对象，其中包含库存锁定是否成功的信息。
     */
    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo) {
        try {
            // 尝试锁定库存
            Boolean lockStock = wareSkuService.orderLockStock(vo);
            log.info("库存锁定结果：{}", lockStock);
            // 返回锁定结果
            return R.ok().setData(lockStock);
        } catch (NoStockException e) {
            // 如果库存不足，则记录日志并返回相应的错误信息
            log.warn("库存锁定失败，订单回滚");
            return R.error(BizCodeEnume.NO_STOCK_EXCEPTION.getCode(), BizCodeEnume.NO_STOCK_EXCEPTION.getMsg());
        }

    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id) {
        WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids) {
        wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 查询指定SKU是否具有库存
     *
     * @param skuIds 需要查询库存的SKU编号列表，以Long型列表传入
     * @return 返回一个R对象，其中包含查询结果。如果成功，R对象的data字段将是一个包含每个SKU是否有库存信息的列表。
     */
    @PostMapping("/hasstock")
    public R getSkuHasStock(@RequestBody List<Long> skuIds) {
        log.info("查询库存，skuIds:{}", skuIds);
        // 调用wareSkuService服务，查询指定skuIds的库存情况
        List<SkuHasStockVo> vos = wareSkuService.getSkuHasStock(skuIds);

        // 返回查询结果

        return R.ok().setData(vos);
    }


}
