package com.atguigu.gulimall.product.app;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 属性分组
 *
 * @author Rain^
 * @email 843524258@qq.com
 * @date 2019-10-01 22:50:32
 */
@RestController
@RequestMapping("product/attrgroup")
@Slf4j
public class AttrGroupController {
    @Resource
    private AttrGroupService attrGroupService;
    @Resource
    private CategoryService categoryService;
    @Resource
    AttrService attrService;
    @Resource
    AttrAttrgroupRelationService relationService;

    /**
     * 添加属性与属性组的关系
     *
     * @param vos 属性组关系的集合，包含需要添加的属性和属性组的信息
     * @return 返回操作结果，成功返回ok
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos) {
        log.info("添加属性与属性组的关系：{}", vos);

        // 批量保存属性与属性组的关系
        relationService.saveBatch(vos);
        return R.ok();
    }

    /**
     * 根据分类ID获取带有属性的属性分组信息
     *
     * @param catelogId 分类ID
     * @return 返回一个包含属性分组及其属性信息的列表
     */
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long catelogId) {
        log.info("根据分类ID获取带有属性的属性分组信息：{}", catelogId);

        // 通过分类ID查询该分类下的所有属性分组及其属性信息
        List<AttrGroupWithAttrsVo> vos = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);

        return R.ok().put("data", vos);
    }


    /**
     * 获取指定属性组关联的属性信息
     *
     * @param attrgroupId 属性组的ID，用于查询该属性组关联的属性信息
     * @return 返回一个包含属性信息的R对象，其中data字段为属性实体列表
     */
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId) {
        log.info("获取指定属性组关联的属性信息：{}", attrgroupId);
        // 通过属性组ID查询关联的属性信息
        List<AttrEntity> entities = attrService.getRelationAttr(attrgroupId);
        // 将查询到的属性信息放入响应对象中返回
        return R.ok().put("data", entities);
    }

    /**
     * 查询指定属性组中未关联的属性信息
     *
     * @param attrgroupId 属性组的ID，用于指定要查询的属性组
     * @param params      其他查询参数，以Map形式传递，可用于进一步筛选未关联的属性
     * @return R对象，其中包含查询结果的分页信息。返回码为200表示成功，"page"字段包含分页数据。
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId,
                            @RequestParam Map<String, Object> params) {
        log.info("查询指定属性组中未关联的属性信息：{}", attrgroupId);
        // 调用attrService获取未关联属性的分页信息
        PageUtils page = attrService.getNoRelationAttr(params, attrgroupId);
        // 将分页信息包装在R对象中返回
        return R.ok().put("page", page);
    }

    /**
     * 删除属性与属性组的关系。
     *
     * @param vos 属性组关系对象数组，包含需要删除的关系信息。
     * @return 返回操作结果，如果操作成功，则返回成功的标识。
     */
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] vos) {
        log.info("删除属性与属性组的关系：{}", vos);
        // 调用服务层方法，删除属性与属性组的关系
        attrService.deleteRelation(vos);
        // 返回操作成功的响应
        return R.ok();
    }

    /**
     * 获取指定分类目录下的属性组列表
     *
     * @param params    查询参数，可以包含页码、每页数量等信息
     * @param catelogId 分类目录的ID，用于指定查询的范围
     * @return 返回一个包含查询结果的页面信息的对象
     */
    @GetMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catelogId") Long catelogId) {
        log.info("获取指定分类目录下的属性组列表：catelogId = {}", catelogId);
        // 根据查询参数和分类目录ID查询属性组信息，返回分页结果
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        log.info("查询结果：总记录数 = {}", page.getTotalCount());

        // 将查询结果包装成成功响应并返回
        return R.ok().put("page", page);
    }


    /**
     * 获取属性组的信息
     *
     * @param attrGroupId 属性组的ID，作为路径变量传递
     * @return 返回一个包含属性组信息的响应对象
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        log.info("获取属性组的信息：attrGroupId = {}", attrGroupId);
        // 通过属性组ID获取属性组信息
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        // 获取属性组所属的目录ID
        Long catelogId = attrGroup.getCatelogId();
        // 查找该目录ID的路径
        Long[] path = categoryService.findCatelogPath(catelogId);

        // 设置属性组的目录路径
        attrGroup.setCatelogPath(path);

        // 返回成功响应，并包含属性组信息
        return R.ok().put("attrGroup", attrGroup);
    }


    /**
     * 保存属性组信息
     *
     * @param attrGroup 属性组实体对象，包含属性组的详细信息，通过RequestBody接收前端传来的JSON数据
     * @return 返回操作结果，如果操作成功，返回一个包含成功标识的基本结果对象
     */
    @PostMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        log.info("保存属性组信息：{}", attrGroup);
        // 调用服务层方法，保存属性组信息
        attrGroupService.save(attrGroup);

        // 返回操作成功的结果
        return R.ok();
    }


    /**
     * 修改属性组信息
     *
     * @param attrGroup 属性组实体对象，包含需要更新的属性组信息
     * @return 返回操作结果，成功返回R.ok()表示操作成功
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        log.info("修改属性组信息：{}", attrGroup);
        // 通过attrGroupService更新属性组信息
        attrGroupService.updateById(attrGroup);

        // 返回操作成功的响应
        return R.ok();
    }


    /**
     * 删除属性组
     *
     * @param attrGroupIds 属性组ID数组，通过RequestBody接收前端传来的多个属性组ID
     * @return 返回操作结果，若删除成功则返回成功信息
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        log.info("删除属性组：{}", attrGroupIds);
        // 根据传入的ID数组删除属性组
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok(); // 返回操作成功的响应
    }


}
