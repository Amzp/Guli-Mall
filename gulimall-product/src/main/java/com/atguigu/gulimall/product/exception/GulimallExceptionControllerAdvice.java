package com.atguigu.gulimall.product.exception;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * 集中处理所有异常
 */
@Slf4j
/**
 * 该注解用于定义一个全局异常处理控制器，它会捕获指定包下所有控制器层抛出的异常。
 * @param basePackages 指定需要扫描的控制器所在的包，此处为"com.atguigu.gulimall.product.controller"。
 */
@RestControllerAdvice(basePackages = "com.atguigu.gulimall.product.controller")

public class GulimallExceptionControllerAdvice {


    /**
     * 处理方法参数不合法的异常。
     * 当前端提交的参数不满足后端验证要求时，抛出MethodArgumentNotValidException异常，此方法负责捕获并响应该异常，返回具体的错误信息。
     *
     * @param e MethodArgumentNotValidException 异常对象，包含验证失败的详细信息。
     * @return R 返回一个结果对象，其中包含业务错误码、错误消息和具体的字段错误信息。
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleVaildException(MethodArgumentNotValidException e) {
        log.error("数据校验出现问题{}，异常类型：{}", e.getMessage(), e.getClass()); // 记录异常信息，便于问题排查。

        BindingResult bindingResult = e.getBindingResult(); // 从异常中获取验证结果对象。

        Map<String, String> errorMap = new HashMap<>(); // 准备用于存放字段错误信息的映射。

        // 遍历所有字段错误，并将其添加到errorMap中。
        bindingResult
                .getFieldErrors()
                .forEach((fieldError) -> {
                    // 将出错字段和其错误信息添加到错误映射表中
                    errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
                });

        // 返回包含业务错误码、错误消息和字段错误信息的结果对象。
        return R.error(BizCodeEnume.VAILD_EXCEPTION.getCode(), BizCodeEnume.VAILD_EXCEPTION.getMsg())
                .put("data", errorMap);
    }

    /**
     * 处理所有未被其他异常处理器捕获的异常。
     *
     * @param throwable 发生的异常对象。
     * @return 返回一个代表操作失败的结果对象，其中包含了业务错误码和错误信息。
     */
    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable) {
        // 记录异常信息
        log.error("错误：", throwable);
        // 返回一个通用的未知异常错误结果
        return R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(), BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
    }


}
