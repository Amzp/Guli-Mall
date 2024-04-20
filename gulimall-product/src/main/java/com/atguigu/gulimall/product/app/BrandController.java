package com.atguigu.gulimall.product.app;

import java.util.*;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatusGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 品牌
 *
 * @author Rain^
 * @email 843524258@qq.com
 * @date 2019-10-01 22:50:32
 */
@RestController
@RequestMapping("product/brand")
@Slf4j
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 查询品牌列表
     *
     * @param params 查询参数，包括分页和过滤条件等。该参数为一个Map，其中可以包含多种查询条件和分页信息，
     *               具体的键值对依赖于具体的业务逻辑和查询需求。
     * @return 返回一个包含查询结果的包装对象R，其中包含了分页信息和品牌数据。R对象通常是一个自定义的返回
     * 类型，可以包含成功状态、错误信息以及具体的查询结果。
     */
    @GetMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params) {
        log.info("查询品牌列表，参数：{}", params);

        // 调用品牌服务，根据传入的参数查询品牌列表的信息，并返回分页数据
        PageUtils page = brandService.queryPage(params);

        // 将查询结果包装成R对象，并将分页信息和品牌数据放入其中，然后返回
        return R.ok().put("page", page);
    }


    /**
     * 查询品牌信息
     *
     * @param brandId 品牌ID，路径变量
     * @return 返回品牌信息，包括品牌本身
     */
    @GetMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId) {
        // 输入验证
        if (brandId == null || brandId <= 0) {
            return R.error("品牌ID不合法");
        }

        try {
            log.info("查询品牌信息，参数：{}", brandId); // 记录查询品牌信息的日志

            BrandEntity brand = brandService.getById(brandId); // 通过品牌ID从服务中获取品牌信息

            // 异常处理：例如品牌不存在的情况
            if (brand == null) {
                return R.error("品牌信息不存在");
            }

            return R.ok().put("brand", brand); // 返回成功响应，并包含品牌信息
        } catch (Exception e) {
            log.error("查询品牌信息异常，参数：{}, 异常：{}", brandId, e.getMessage());
            return R.error("查询品牌信息异常");
        }
    }


    /**
     * 保存品牌信息
     *
     * @param brand 品牌实体，包含品牌相关的详细信息，需要进行验证确保数据的完整性和合法性。
     * @return 返回操作结果，若操作成功，则返回一个表示操作成功的标识。
     */
    @PostMapping("/save")
    //@RequiresPermissions("product:brand:save")
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand) {
        // 记录保存品牌信息的操作日志
        log.info("保存品牌信息，参数：{}", brand);

        // 调用品牌服务层方法，保存品牌信息
        brandService.save(brand);

        // 返回操作成功的响应
        return R.ok();
    }


    /**
     * 修改品牌信息
     *
     * @param brand 需要更新的品牌实体，通过RequestBody接收前端传来的JSON数据。
     *              使用@Validated注解对brand参数进行验证，确保更新的数据符合UpdateGroup验证组的要求。
     * @return 返回操作结果，如果操作成功，则返回R.ok()，代表操作成功。
     */
    @PostMapping("/update")
    //@RequiresPermissions("product:brand:update") // 此注解用于权限控制，表示更新品牌信息需要相应的权限
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand) {
        log.info("修改品牌信息，参数：{}", brand);

        brandService.updateDetail(brand); // 调用brandService更新品牌信息的详细内容

        return R.ok(); // 返回操作成功的响应
    }


    /**
     * 修改品牌的状态
     *
     * @param brand 品牌实体，包含需要更新的状态信息
     * @return 返回操作结果，成功则返回一个包含成功信息的R对象
     */
    @PostMapping("/update/status")
    //@RequiresPermissions("product:brand:update")
    public R updateStatus(@Validated(UpdateStatusGroup.class) @RequestBody BrandEntity brand) {
        log.info("修改品牌状态，参数：{}", brand); // 记录修改品牌状态的操作日志
        brandService.updateById(brand); // 根据品牌实体更新数据库中的品牌信息

        return R.ok(); // 返回操作成功的响应
    }


    /**
     * 删除品牌
     *
     * @param brandIdList 品牌ID数组，需通过RequestBody接收，表示需要删除的品牌ID列表
     * @return 返回操作结果，成功返回R.ok()，失败返回错误信息
     */
    @PostMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R deleteBrands(@RequestBody List<Long> brandIdList) {
        // 先进行输入验证
        if (brandIdList == null || brandIdList.isEmpty()) {
            log.error("删除品牌失败，输入的品牌ID列表不能为空");
            throw new IllegalArgumentException("品牌ID列表不能为空");
        }
        // 校验brandIdList中是否包含null元素
        if (brandIdList.stream().anyMatch(Objects::isNull)) {
            log.error("删除品牌失败，品牌ID列表中包含null元素");
            throw new IllegalArgumentException("品牌ID列表中不能包含null元素");
        }

        // 记录删除操作的日志
        log.info("删除品牌，参数：{}", brandIdList);
        try {
            // 根据提供的品牌ID列表，删除对应的品牌记录
            brandService.removeByIds(brandIdList);

            // 返回成功信息
            return R.ok("品牌删除成功");
        } catch (Exception e) {
            // 捕获异常，返回失败信息
            log.error("删除品牌失败，异常：{}", e.getMessage());
            return R.error("品牌删除失败：" + e.getMessage());
        }
    }

}
