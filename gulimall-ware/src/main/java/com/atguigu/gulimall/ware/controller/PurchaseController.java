package com.atguigu.gulimall.ware.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.atguigu.gulimall.ware.vo.MergeVo;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;

import javax.annotation.Resource;


/**
 * 采购信息
 *
 * @author Rain^
 * @email 843524258@qq.com
 * @date 2019-11-17 13:50:10
 */
@RestController
@RequestMapping("ware/purchase")
@Slf4j
public class PurchaseController {
    @Resource
    private PurchaseService purchaseService;

    /**
     * 完成采购操作。
     * 该接口用于标记采购流程为完成状态。
     *
     * @param doneVo 包含采购完成信息的Vo对象，通过RequestBody接收前端传来的JSON数据。
     * @return 返回一个表示操作结果的R对象，通常表示操作是否成功。
     */
    @PostMapping("/done")
    public R finish(@RequestBody PurchaseDoneVo doneVo){
        log.info("采购单完成信息： doneVo = {}", doneVo);
        // 调用采购服务，完成采购操作
        purchaseService.done(doneVo);
        // 返回操作成功的响应
        return R.ok();
    }

    /**
     * 领取采购单接口
     * 该方法用于将指定的采购单领取到个人或部门，标记采购单为已接收状态。
     * @param ids 需要领取的采购单ID列表，以Long型列表传入。
     * @return 返回操作结果，成功返回R.ok()。
     */
    @PostMapping("/received")
    public R received(@RequestBody List<Long> ids){
        log.info("需要领取的采购单ID列表： ids = {}", ids);
        // 调用采购服务，将指定采购单标记为已接收
        purchaseService.received(ids);
        // 返回操作成功的响应
        return R.ok();
    }


    /**
     * 合并采购单接口
     * <p>
     * 接收前端传来的合并采购信息，将多个采购单合并为一个采购单。
     * </p>
     * @param mergeVo 合并采购的请求体，包含需要合并的采购单信息。
     * @return 返回操作结果，成功则返回操作成功的标识。
     */
    @PostMapping("/merge")
    public R merge(@RequestBody MergeVo mergeVo){
        log.info("合并采购单信息： mergeVo = {}", mergeVo);
        // 调用采购服务，执行采购单合并操作
        purchaseService.mergePurchase(mergeVo);
        return R.ok(); // 返回操作成功的响应
    }

    /**
     * 查询未接收的采购单列表
     *
     * @param params 包含查询条件的参数映射
     * @return 返回包含查询结果的R对象，其中page属性为分页查询结果
     */
    @RequestMapping("/unreceive/list")
    public R unreceivelist(@RequestParam Map<String, Object> params){
        log.info("查询未接收的采购单列表，参数： params = {}", params);
        // 调用purchaseService查询未接收的采购单页面信息
        PageUtils page = purchaseService.queryPageUnreceivePurchase(params);

        // 将查询结果包装在R对象中返回
        return R.ok().put("page", page);
    }

    /**
     * 获取采购单列表
     * @param params 查询参数，可以包含页码、每页数量等信息
     * @return 返回一个包含采购单列表信息的响应对象
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        log.info("获取采购单列表，参数： params = {}", params);
        // 查询采购单列表信息
        PageUtils page = purchaseService.queryPage(params);

        // 将查询结果包装在响应对象中返回
        return R.ok().put("page", page);
    }



    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id){

		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase){
        purchase.setUpdateTime(new Date());
        purchase.setCreateTime(new Date());
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
