/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package com.atguigu.common.utils;

import com.atguigu.common.xss.SQLFilter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * 查询参数类，提供获取分页信息和排序功能的方法。
 *
 * @author Mark sunlightcs@gmail.com
 * @param <T> 查询结果的类型
 */
public class Query<T> {

    /**
     * 根据参数获取分页对象。
     *
     * @param params 查询参数，包括分页参数（page、limit）和排序参数（order_field、order）等。
     * @return 返回配置好的IPage分页对象。
     */
    public IPage<T> getPage(Map<String, Object> params) {
        return this.getPage(params, null, false);
    }

    /**
     * 根据参数获取分页对象，支持默认排序字段和排序方式。
     *
     * @param params           查询参数，包括分页参数（page、limit）和排序参数（order_field、order）等。
     * @param defaultOrderField 默认排序字段，如果请求中没有提供排序字段，则使用此字段进行排序。
     * @param isAsc            默认排序方式，配合defaultOrderField使用，指定是升序还是降序。
     * @return 返回配置好的IPage分页对象。
     */
    public IPage<T> getPage(Map<String, Object> params, String defaultOrderField, boolean isAsc) {
        // 初始化分页参数
        long curPage = 1;
        long limit = 10;

        // 从参数中解析当前页和每页数量
        if(params.get(Constant.PAGE) != null){
            curPage = Long.parseLong((String)params.get(Constant.PAGE));
        }
        if(params.get(Constant.LIMIT) != null){
            limit = Long.parseLong((String)params.get(Constant.LIMIT));
        }

        // 创建分页对象
        Page<T> page = new Page<>(curPage, limit);

        // 将分页对象加入参数中，以便在后续处理中使用
        params.put(Constant.PAGE, page);

        // 排序字段处理和排序方式判断
        // 防止SQL注入，对排序字段进行过滤
        String orderField = SQLFilter.sqlInject((String)params.get(Constant.ORDER_FIELD));
        String order = (String)params.get(Constant.ORDER);

        // 如果存在前端指定的排序字段和方式，则按其排序
        if(StringUtils.isNotEmpty(orderField) && StringUtils.isNotEmpty(order)){
            if(Constant.ASC.equalsIgnoreCase(order)) {
                return  page.addOrder(OrderItem.asc(orderField));
            }else {
                return page.addOrder(OrderItem.desc(orderField));
            }
        }

        // 如果没有指定排序字段，则检查是否有默认排序字段
        if(StringUtils.isBlank(defaultOrderField)){
            return page;
        }

        // 应用默认排序
        if(isAsc) {
            page.addOrder(OrderItem.asc(defaultOrderField));
        }else {
            page.addOrder(OrderItem.desc(defaultOrderField));
        }

        return page;
    }
}

