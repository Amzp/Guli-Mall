package com.atguigu.gulimall.order.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.vo.MemberRespVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ClassName: LoginUserInterceptor
 * Package: com.atguigu.gulimall.order.interceptor
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/28 下午4:04
 * @Version 1.0
 */
@Component
@Slf4j
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute != null){
            loginUser.set(attribute);
            log.info("拦截器：用户已经登录，用户id为：{}",attribute.getId());
            return true;

        }else {
            request.getSession().setAttribute("msg","请先进行登录");
            log.info("拦截器：用户未登录，请先进行登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }

    }
}
