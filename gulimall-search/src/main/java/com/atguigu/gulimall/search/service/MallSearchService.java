package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;

/**
 * ClassName: MallSearchService
 * Package: com.atguigu.gulimall.search.service
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/21 下午9:48
 * @Version 1.0
 */
public interface MallSearchService {

    SearchResult search(SearchParam param);
}
