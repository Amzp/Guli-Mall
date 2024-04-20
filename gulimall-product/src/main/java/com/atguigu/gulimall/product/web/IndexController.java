package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catalog2Vo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * ClassName: IndexController
 * Package: com.atguigu.gulimall.product.web
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/19 下午3:51
 * @Version 1.0
 */
@Controller
@Slf4j
public class IndexController {
    @Resource
    private CategoryService categoryService;


    /**
     * 处理首页请求，将所有的1级分类信息添加到模型中，并返回首页的逻辑视图名称。
     *
     * @param model 用于在视图中展示数据的模型对象，此处用于存放分类信息。
     * @return 返回逻辑视图名称"index"，对应的实体页面为templates目录下的index.html。
     */
    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        log.info("请求首页");
        // 从服务中查询所有的1级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();

        // 将查询到的分类信息添加到模型中，供视图使用
        model.addAttribute("categorys", categoryEntities);

        // 返回逻辑视图名称，由视图解析器负责将此名称解析为实际的物理视图路径
        return "index";
    }

    /**
     * 获取分类数据的JSON格式
     * <p>
     * 该接口不需要参数，通过GET请求访问，返回一个Map对象，其中键是分类的名称，值是对应分类下的商品目录列表。
     * 这个方法主要用于前端展示商品分类信息，方便用户浏览和选择商品。
     *
     * @return 返回一个Map<String, List < Catalog2Vo>>，其中包含了三级分类的数据结构。
     */
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        log.info("查询三级分类数据");
        // 调用categoryService获取分类数据
        return categoryService.getCatalogJson();
    }
}
