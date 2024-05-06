package com.atguigu.common.annotation;

import java.lang.annotation.*;

/**
 * ClassName: LogInfo
 * Package: com.atguigu.gulimall.order.annotation
 * Description:Log注解类，用于标记需要进行日志记录的方法或参数。可以被应用在方法级别或参数级别，以提供日志记录的额外信息。
 *
 * @Author Rainbow
 * @Create 2024/5/2 下午9:12
 * @Version 1.0
 */
@Target({ ElementType.PARAMETER, ElementType.METHOD })  // 该注解可以用于方法或参数上
@Retention(RetentionPolicy.RUNTIME) // 该注解的生命周期为运行时，即在运行时可以通过反射访问到该注解
@Documented // 该注解会被包含在javadoc中
public @interface LogInfo {

    /**
     * name属性，用于指定记录日志时的模块名称。
     * 默认值为空字符串，即不指定模块名称。
     */
    String name() default "";
}
