package com.atguigu.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;

import javax.validation.Valid;

/**
 * 商品三级分类
 *
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-01 22:50:32
 */
@RestController
@RequestMapping("product/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 查出所有分类以及子分类，以树形结构组装起来
     */
    @RequestMapping("/list/tree")
    public R list(){
        log.info("查出所有分类以及子分类，以树形结构组装起来...");
        List<CategoryEntity> entities = categoryService.listWithTree();

        return R.ok().put("data", entities);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    //@RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("data", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return R.ok();
    }

    @RequestMapping("/update/sort")
    //@RequiresPermissions("product:category:update")
    public R updateSort(@RequestBody CategoryEntity[] category){
        categoryService.updateBatchById(Arrays.asList(category));
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity category){
		categoryService.updateCascade(category);
        return R.ok();
    }


    /**
     * 删除指定分类ID的产品分类信息
     *
     * @param catIds 需要删除的产品分类ID数组，通过POST请求的请求体以JSON格式传递
     * @return 返回操作结果，成功操作返回R.ok()，表示操作成功
     */
    @DeleteMapping("/delete")
    public R deleteProductCategoriesByIds(@RequestBody List<Long> catIds){
        log.info("尝试删除分类ID：{} 的产品分类信息...", catIds);

        // 输入验证：确保catIds不为空且包含合法的ID
        if (catIds == null || catIds.isEmpty()) {
            log.error("删除请求中未提供任何分类ID");
            return R.error("删除请求中未提供任何分类ID");
        }
        // 假设ID为正数且不为0的为合法ID，根据实际情况调整
        for (Long id : catIds) {
            if (id <= 0) {
                log.error("分类ID {} 不合法", id);
                return R.error("分类ID不合法");
            }
        }

        try {
            // 调用categoryService的removeMenuByIds方法，根据提供的ID数组删除相应的分类信息
            categoryService.removeMenuByIds(catIds);

            log.info("成功删除分类ID：{} 的产品分类信息", catIds);
            // 返回操作成功的响应
            return R.ok();
        } catch (Exception e) {
            log.error("删除分类ID {} 时发生异常：{}", catIds, e.getMessage());
            // 返回操作失败的响应，包含错误信息
            return R.error("删除操作失败：" + e.getMessage());
        }
    }


}
