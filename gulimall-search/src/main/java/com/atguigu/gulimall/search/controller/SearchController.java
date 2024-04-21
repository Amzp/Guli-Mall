package com.atguigu.gulimall.search.controller;


import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;


/**
 * ClassName: SearchController
 * Package: com.atguigu.gulimall.search.controller
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/21 下午7:52
 * @Version 1.0
 */
@Controller
@Slf4j
public class SearchController {

    @Resource
    private MallSearchService mallSearchService;

    /**
     * 处理用户请求，展示商品列表页面。
     *
     * @param param 用户的搜索参数，用于商品搜索。
     * @param model 用于在视图和控制器之间传递数据的模型对象。
     * @return 返回页面视图名，此处为"list"，即展示商品列表的页面。
     */
    @GetMapping(value = {"/list.html"})
    public String listPage(SearchParam param, Model model) {
        // 执行商品搜索，根据搜索参数获取搜索结果
        SearchResult result = mallSearchService.search(param);
        // 将搜索结果添加到模型中，以便在页面上展示
        model.addAttribute("result", result);
        // 返回页面视图名
        return "list";
    }
}
