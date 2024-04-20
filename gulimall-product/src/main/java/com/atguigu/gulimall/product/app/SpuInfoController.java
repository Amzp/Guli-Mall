package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.Map;

import com.atguigu.gulimall.product.vo.SpuSaveVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.atguigu.gulimall.product.service.SpuInfoService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;

import javax.annotation.Resource;


/**
 * spu信息
 *
 * @author Rain^
 * @email 843524258@qq.com
 * @date 2019-10-01 22:50:32
 */
@RestController
@RequestMapping("product/spuinfo")
@Slf4j
public class SpuInfoController {
    @Resource
    private SpuInfoService spuInfoService;

    /**
     * 商品上架，更新Spu信息
     *
     * @param spuId 商品主体信息ID，用于指定需要上架更新的SPU
     * @return 返回操作结果，成功返回OK
     */
    @PostMapping("/{spuId}/up")
    public R spuUp(@PathVariable("spuId") Long spuId){
        log.info("商品上架，更新Spu信息：spuId = {}", spuId);
        // 调用服务层方法，执行SPU更新操作
        spuInfoService.up(spuId);

        // 返回成功响应
        return R.ok();
    }


    /**
     * 查询SPU信息列表
     *
     * @param params 查询条件参数，封装了前端传来的各种查询条件
     * @return 返回查询结果的包装对象，包含分页信息和查询到的数据
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:spuinfo:list")
    public R list(@RequestParam Map<String, Object> params) {
        log.info("查询SPU信息列表：{}", params);
        // 根据条件查询SPU信息，并返回分页结果
        PageUtils page = spuInfoService.queryPageByCondition(params);

        // 将查询结果包装成R对象，返回给前端
        return R.ok().put("page", page);
    }


    /**
     * 获取指定ID的产品基本信息
     *
     * @param id 产品的唯一标识符
     * @return 返回一个包含产品信息的响应对象
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("product:spuinfo:info")
    public R info(@PathVariable("id") Long id) {
        log.info("获取指定ID的产品基本信息：{}", id);
        // 通过ID从服务中获取SPU信息
        SpuInfoEntity spuInfo = spuInfoService.getById(id);

        // 将获取到的SPU信息放入响应对象中并返回
        return R.ok().put("spuInfo", spuInfo);
    }


    /**
     * 保存SPU（商品）信息
     *
     * @param vo SPU的保存视图对象，包含需要保存的SPU信息
     * @return 返回操作结果，成功则为R.ok()
     */
    @PostMapping("/save")
    //@RequiresPermissions("product:spuinfo:save")
    public R save(@RequestBody SpuSaveVo vo) {
        log.info("保存SPU信息：{}", vo);
        // 调用服务层方法，保存SPU信息
        spuInfoService.saveSpuInfo(vo);

        return R.ok();
    }


    /**
     * 修改SPU信息
     *
     * @param spuInfo 要更新的SPU信息实体
     * @return 返回操作结果，成功返回OK
     */
    @PostMapping("/update")
    //@RequiresPermissions("product:spuinfo:update")
    public R update(@RequestBody SpuInfoEntity spuInfo) {
        log.info("修改SPU信息：{}", spuInfo);
        // 通过ID更新SPU信息
        spuInfoService.updateById(spuInfo);

        return R.ok();
    }


    /**
     * 删除指定的SPU信息
     *
     * @param ids 要删除的SPU的ID数组，通过RequestBody接收
     * @return 返回操作结果，成功返回OK
     */
    @PostMapping("/delete")
    //@RequiresPermissions("product:spuinfo:delete")
    public R delete(@RequestBody Long[] ids) {
        log.info("删除指定的SPU信息：{}", ids);
        // 批量删除指定ID的SPU信息
        spuInfoService.removeByIds(Arrays.asList(ids));

        // 返回操作成功的响应
        return R.ok();
    }


}
