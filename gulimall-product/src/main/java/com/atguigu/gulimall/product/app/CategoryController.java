package com.atguigu.gulimall.product.app;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.common.utils.R;

/**
 * 商品三级分类
 *
 * @author Rain^
 * @email 843524258@qq.com
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
     *
     * 本方法不接受任何参数。
     *
     * @return 返回一个包含所有分类及其子分类的树形结构的信息。
     */
    @GetMapping("/list/tree")
    public R list() {
        Instant start = Instant.now();
        log.info("开始查询所有分类及其子分类的树形结构...");

        // 从categoryService获取所有分类及其子分类的树形结构
        List<CategoryEntity> entities;
        try {
            entities = categoryService.listWithTree();
        } catch (Exception e) {
            log.error("查询分类树形结构时发生异常：", e);
            return R.error("查询分类树形结构失败");
        }

        Instant end = Instant.now();
        log.info("查询所有分类及其子分类的树形结构完成，耗时：{} 毫秒", Duration.between(start, end).toMillis());

        // 将获取到的分类树形结构数据放入响应体中返回
        return R.ok().put("data", entities);
    }



    /**
     * 根据分类ID查询分类信息。
     * <p>
     *
     * @param catId - 商品分类的ID
     *              <p>
     * @Return： R对象，包含查询到的分类信息。如果查询成功，R对象的data字段将包含CategoryEntity实例。
     */
    @GetMapping("/info/{catId}")
    //@RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId) {
        log.info("根据分类ID查询分类信息：{}", catId); // 记录查询分类信息的日志
        // 输入验证
        if (catId == null || catId <= 0) {
            return R.error("分类ID不合法");
        }
        // 通过分类ID获取分类信息
        CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("data", category); // 返回查询结果
    }


    /**
     * 保存分类信息
     *
     * @param category 分类实体对象，通过RequestBody接收前端传来的JSON数据
     * @return 返回操作结果，成功则返回一个包含成功标识的R对象
     */
    @PostMapping("/save")
    //@RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category) {
        log.info("保存分类信息：{}", category);
        // 调用categoryService保存分类信息
        categoryService.save(category);

        // 返回操作成功的响应
        return R.ok();
    }


    /**
     * 更新商品分类的排序信息
     *
     * @param category 一个CategoryEntity对象数组，代表需要更新排序信息的商品分类
     * @return 返回一个表示操作结果的R对象，如果操作成功，则ok()方法返回一个包含成功信息的R对象
     */
    @PostMapping("/update/sort")
    public R updateSort(@RequestBody List<CategoryEntity> category) {
        Logger log = LoggerFactory.getLogger(this.getClass());
        log.info("开始更新商品分类的排序信息, 分类数量: {}", category.size());

        // 验证输入
        if (category == null || category.isEmpty()) {
            log.warn("传入的分类列表为空，将返回操作失败");
            return R.error("传入的分类列表不能为空");
        }

        try {
            // 批量更新分类信息
            categoryService.updateBatchById(category);
            log.info("成功更新了{}个商品分类的排序信息", category.size());
            return R.ok();
        } catch (Exception e) {
            log.error("更新商品分类排序信息时发生异常: {}", e.getMessage());
            // 这里根据实际情况返回具体的错误信息，或者考虑抛出自定义异常
            return R.error("更新操作失败");
        }
    }

    /**
     * 修改分类信息
     *
     * @param category 分类实体，包含需要修改的分类信息。该实体应包含分类的各种属性，如分类ID、分类名称等。
     * @return 操作结果，成功返回ok，失败返回错误信息。通过返回不同的R对象来表示操作的成功与否。
     */
    @PostMapping("/update")
    public R update(@RequestBody CategoryEntity category) {
        try {
            // 验证分类实体信息的合法性，确保传入的分类实体不为空且分类ID有效
            if (category == null || category.getCatId() == null) {
                log.error("分类信息或分类ID不能为空");
                return R.error("分类信息或分类ID不能为空");
            }

            // 记录日志时，避免直接记录敏感信息，这里仅记录分类ID
            log.info("修改分类信息，ID：{}", category.getCatId());

            // 调用服务层方法，更新分类信息。updateCascade方法会级联更新分类相关的所有信息。
            categoryService.updateCascade(category);

            return R.ok();
        } catch (Exception e) {
            // 捕获并处理异常，返回通用异常信息，也可根据异常类型返回更具体的错误信息
            log.error("修改分类信息异常：", e);
            return R.error("修改分类信息失败");
        }
    }



    /**
     * 删除指定分类ID的产品分类信息
     *
     * @param catIds 需要删除的产品分类ID数组，通过POST请求的请求体以JSON格式传递
     * @return 返回操作结果，成功操作返回R.ok()，表示操作成功
     */
    @PostMapping("/delete")
    public R deleteProductCategoriesByIds(@RequestBody List<Long> catIds) {
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
