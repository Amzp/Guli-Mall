/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package com.atguigu.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;


/**
 * R类继承自HashMap<String, Object>用于方便地构建和返回包含特定代码和消息的结果对象。
 */
public class R extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public R setData(Object data) {
        this.put("data", data);
        return this;
    }

    /**
     * 根据给定的键获取数据，并将其转换为指定的泛型类型。
     *
     * @param key 用于获取数据的键。
     * @param typeReference 指定数据应被转换成的泛型类型引用。
     * @return 转换后的数据对象，其类型由typeReference指定。
     * @param <T> 数据的泛型类型。
     */
    public <T> T getData(String key, TypeReference<T> typeReference) {

        // 通过键获取数据
        Object data = get(key);

        // 将获取到的数据转换为JSON字符串
        String jsonString = JSON.toJSONString(data);

        // 将JSON字符串转换为指定的泛型类型
        T t = JSON.parseObject(jsonString, typeReference);
        return t;
    }

    /**
     * 从特定数据源获取数据，并将其转换为指定类型。
     *
     * @param typeReference 用于指定目标数据类型的 TypeReference 实例。这允许泛型类型的精确转换。
     * @return 转换后的数据对象，其类型由 typeReference 指定。
     * @param <T> 泛型参数，指定要返回的数据类型。
     */
    public <T> T getData(TypeReference<T> typeReference) {
        // 从数据源获取数据，这里假设存在一个 get 方法，该方法以键名 "data" 获取数据
        Object data = get("data");
        // 将获取到的数据对象转换为 JSON 字符串
        String jsonString = JSON.toJSONString(data);
        // 使用 JSON 库，将 JSON 字符串转换为指定的泛型类型 T
        T t = JSON.parseObject(jsonString, typeReference);
        return t;
    }

    /**
     * 构造函数初始化一个包含默认成功代码和消息的新R对象。
     */
    public R() {
        put("code", 0);
        put("msg", "success");
    }

    /**
     * 创建一个包含错误代码和默认错误消息的R对象。
     *
     * @return 返回一个初始化为服务器内部错误代码和“未知异常，请联系管理员”消息的R实例。
     */
    public static R error() {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
    }

    /**
     * 创建一个包含指定错误消息的R对象。
     *
     * @param msg 错误消息。
     * @return 返回一个初始化为服务器内部错误代码和指定错误消息的R实例。
     */
    public static R error(String msg) {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
    }

    /**
     * 创建一个包含指定错误代码和消息的R对象。
     *
     * @param code 错误代码。
     * @param msg  错误消息。
     * @return 返回一个初始化为指定错误代码和消息的R实例。
     */
    public static R error(int code, String msg) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    /**
     * 创建一个包含成功消息的R对象。
     *
     * @param msg 成功消息。
     * @return 返回一个初始化为成功消息的R实例。
     */
    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    /**
     * 创建一个包含指定Map中所有键值对的R对象。
     *
     * @param map 包含要添加到R对象的键值对的Map。
     * @return 返回一个包含传入Map所有键值对的R实例。
     */
    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    /**
     * 创建一个空的R对象。
     *
     * @return 返回一个空的R实例。
     */
    public static R ok() {
        return new R();
    }

    /**
     * 向当前R对象中添加一个键值对，并返回当前R对象以支持链式调用。
     *
     * @param key   键。
     * @param value 值。
     * @return 返回当前R对象。
     */
    @Override
    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    /**
     * 获取当前R对象中存储的代码。
     *
     * @return 返回“code”键对应的值，如果不存在则返回null。
     */
    public Integer getCode() {

        return (Integer) this.get("code");
    }
}
