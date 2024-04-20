package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.vo.BrandVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;

import javax.annotation.Resource;

/**
 * 品牌分类关联
 *
 * @author Rain^
 * @email 843524258@qq.com
 * @date 2019-11-17 21:25:25
 */
@RestController
@RequestMapping("product/categorybrandrelation")
@Slf4j
public class CategoryBrandRelationController {
    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 获取指定品牌关联的所有分类列表
     *
     * @param brandId 品牌ID，用于查询与该品牌关联的分类信息
     * @return 返回一个包含查询结果的R对象，其中data字段为品牌关联的分类列表
     */
    @GetMapping("/catelog/list")
    //@RequiresPermissions("product:categorybrandrelation:list")
    public R cateloglist(@RequestParam("brandId") Long brandId) {
        log.info("获取指定品牌关联的所有分类列表，brandId:{}", brandId);
        // 使用品牌ID查询所有关联的分类信息
        List<CategoryBrandRelationEntity> data = categoryBrandRelationService.list(
                new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId)
        );

        // 返回查询结果
        return R.ok().put("data", data);
    }


    /**
     * 获取指定分类关联的所有品牌列表。
     * <p>
     * 请求路径：/product/categorybrandrelation/brands/list
     * <p>
     * 该接口流程涉及三个主要部分：
     * 1、Controller：处理客户端请求，校验请求参数。
     * 2、Service：根据Controller传递的参数进行业务逻辑处理。
     * 3、Controller：接收Service处理后的数据，将其封装成前端指定的视图对象（VO）返回。
     *
     * @param catId 分类的ID，用于查询该分类下关联的品牌。
     * @return 返回品牌列表的信息，其中包含品牌ID和品牌名称。
     */
    @GetMapping("/brands/list")
    public R relationBrandsList(@RequestParam(value = "catId") Long catId) {
        log.info("获取当前分类关联的所有品牌列表，catId:{}", catId);

        // 通过服务层方法获取指定分类ID下的所有品牌实体列表
        List<BrandEntity> vos = categoryBrandRelationService.getBrandsByCatId(catId);

        // 将品牌实体列表转换为品牌视图对象列表，简化前端处理
        List<BrandVo> collect = vos.stream()
                .map(item -> new BrandVo(item.getBrandId(), item.getName()))
                .collect(Collectors.toList());

        // 返回成功响应，并将品牌视图对象列表放在数据字段中
        return R.ok().put("data", collect);
    }


    /**
     * 查询分类品牌关系列表
     *
     * @param params 查询参数，包括分页和过滤条件等
     * @return 返回查询结果的包装对象，包括分页信息和数据列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:categorybrandrelation:list")
    public R list(@RequestParam Map<String, Object> params) {
        log.info("查询分类品牌关系列表，params:{}", params);
        // 调用服务查询分类品牌关系的分页数据
        PageUtils page = categoryBrandRelationService.queryPage(params);

        // 将查询结果包装成R对象返回，其中包含分页信息
        return R.ok().put("page", page);
    }

    /**
     * 获取指定ID的品牌类别关系信息
     *
     * @param id 品牌类别关系的ID
     * @return 返回一个包含品牌类别关系信息的响应对象
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        log.info("获取指定ID的品牌类别关系信息，id:{}", id);
        // 通过ID从服务中获取品牌类别关系实体
        CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        // 将获取到的品牌类别关系实体放入响应对象中并返回
        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存品牌分类关联关系
     *
     * @param categoryBrandRelation 品牌分类关联实体对象，包含品牌与分类的关联信息
     * @return 返回操作结果，成功返回R.ok()，即操作成功的标识
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:categorybrandrelation:save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        log.info("保存品牌分类关联关系，categoryBrandRelation:{}", categoryBrandRelation);

        // 调用服务层方法，保存品牌分类关联关系的详细信息
        categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改商品类别与品牌关系
     * @param categoryBrandRelation 类别品牌关系实体，包含需要更新的关系信息
     * @return 返回操作结果，成功返回R.ok()表示操作成功
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        log.info("修改商品类别与品牌关系，categoryBrandRelation:{}", categoryBrandRelation);
        // 通过服务更新类别品牌关系
        categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok(); // 表示更新操作成功
    }

    /**
     * 删除指定的品牌类别关系
     *
     * @param ids 需要删除的品牌类别关系的ID数组
     * @return 返回操作结果，成功返回OK
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:categorybrandrelation:delete")
    public R delete(@RequestBody Long[] ids) {
        log.info("删除指定的品牌类别关系，ids:{}", (Object) ids);
        // 根据提供的ID数组删除品牌类别关系
        categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
