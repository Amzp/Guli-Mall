package com.atguigu.gulimall.product.app;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.ProductAttrValueService;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 商品属性
 *
 * @author Rain^
 * @email 843524258@qq.com
 * @date 2019-10-01 22:50:32
 */
@RestController
@RequestMapping("product/attr")
@Slf4j
public class AttrController {
    @Resource
    private AttrService attrService;

    @Resource
    ProductAttrValueService productAttrValueService;


    /**
     * 根据SPU ID获取基础属性列表
     *
     * @param spuId 商品规格模型ID，用于查询关联的基础属性列表
     * @return R 返回一个结果对象，其中包含了查询到的基础属性列表
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrlistforspu(@PathVariable("spuId") Long spuId) {



        log.info("根据SPU ID获取基础属性列表：spuId = {}", spuId);
        // 通过productAttrValueService查询指定spuId的基础属性列表
        List<ProductAttrValueEntity> entities = productAttrValueService.baseAttrlistforspu(spuId);

        // 将查询结果包装在R对象中返回
        return R.ok().put("data", entities);
    }

    /**
     * 根据属性类型和目录ID查询基础属性列表
     *
     * @param params    请求参数，包含分页和查询条件信息
     * @param catelogId 目录ID，用于查询特定目录下的属性
     * @param type      属性类型，区分不同的属性类别
     * @return 返回一个包含查询结果的页面信息的对象
     */
    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params,
                          @PathVariable("catelogId") Long catelogId,
                          @PathVariable("attrType") String type) {
        log.info("根据属性类型和目录ID查询基础属性列表：attrType = {}, catelogId = {}", type, catelogId);
        // 调用服务层方法，查询指定条件下的基础属性分页列表
        PageUtils page = attrService.queryBaseAttrPage(params, catelogId, type);
        // 将查询结果包装成成功响应并返回
        return R.ok().put("page", page);
    }


    /**
     * 查询属性列表
     *
     * @param params 包含查询条件的参数映射
     * @return 返回包含查询结果的页面信息
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params) {
        log.info("查询属性列表：{}", params);
        // 查询并返回分页数据
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 获取属性信息
     *
     * @param attrId 属性的ID，用于指定要获取的属性信息
     * @return 返回一个包含属性信息的响应对象
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId) {
        log.info("获取属性信息：attrId = {}", attrId);

        // 通过属性ID获取属性信息，返回一个响应体，其中包含属性的详细信息
        AttrRespVo respVo = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", respVo);
    }


    /**
     * 保存属性信息
     *
     * @param attrVo 属性信息对象，包含属性的详细信息，通过RequestBody接收前端传来的JSON数据
     * @return 返回操作结果，如果操作成功，返回一个包含成功标识的基本结果对象
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attrVo) {
        log.info("保存属性信息：{}", attrVo);
        // 调用服务层方法，保存属性信息
        attrService.saveAttr(attrVo);
        log.info("保存属性信息成功");

        // 返回操作成功的结果
        return R.ok();
    }

    /**
     * 修改属性信息
     *
     * @param attr 要更新的属性对象，包含属性的全部信息
     * @return 返回操作结果，成功返回OK
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVo attr) {
        log.info("修改属性信息：{}", attr);
        // 调用服务层方法，更新属性信息
        attrService.updateAttr(attr);

        return R.ok(); // 返回操作成功的标志
    }


    /**
     * 更新SPU的属性值
     *
     * @param spuId    商品规格ID，用于指定需要更新属性的SPU
     * @param entities 要更新的属性值列表，每个属性包括属性ID、属性值等信息
     * @return 返回操作结果，成功返回OK（R.ok()）
     */
    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId,
                           @RequestBody List<ProductAttrValueEntity> entities) {
        log.info("更新SPU的属性值：spuId = {}, entities = {}", spuId, entities);

        // 调用服务层方法，更新SPU的属性值
        productAttrValueService.updateSpuAttr(spuId, entities);

        // 返回成功响应
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds) {
        attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
