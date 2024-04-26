package com.atguigu.gulimall.cart.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.constant.CartConstant;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.cart.to.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * ClassName: CartInterceptor
 * Package: com.atguigu.gulimall.cart.interceptor
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/26 下午9:46
 * @Version 1.0
 */

@Slf4j
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /**
     * 在目标方法执行前处理请求。
     *
     * @param request  HttpServletRequest对象，代表客户端的HTTP请求
     * @param response HttpServletResponse对象，用于向客户端发送HTTP响应
     * @param handler  将要执行的目标处理器对象
     * @return boolean 返回值，表示是否继续执行后续的拦截器或处理器
     * @throws Exception 抛出异常，处理过程中的任何异常都可以抛出
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();

        HttpSession session = request.getSession();
        // 尝试从session中获取已登录的用户信息
        MemberRespVo memberRespVo = (MemberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);

        if (memberRespVo != null) {
            log.info("用户已登录，用户id：{}", memberRespVo.getId());
            userInfoTo.setUserId(memberRespVo.getId());
        }

        // 检查cookie中是否有临时用户信息
        log.info("用户未登录，拦截请求");
        Cookie[] cookies = request.getCookies(); // 获取请求中的所有cookie
        if (cookies != null) {
            for (Cookie cookie : cookies) { // 遍历所有cookie
                String name = cookie.getName();
                // 如果找到匹配TEMP_USER_COOKIE_NAME的cookie，则处理临时用户信息
                if (name.equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    userInfoTo.setUserKey(cookie.getValue());
                    // 设置临时用户标识
                    userInfoTo.setTempUser(true);
                    log.info("用户未登录，但cookie中存在临时用户，临时用户id：{}", cookie.getValue());
                }
            }
        }

        // 如果没有临时用户信息，生成一个新的临时用户ID
        if (userInfoTo.getUserKey() == null) {
            String uuid = UUID.randomUUID().toString();
            log.info("用户未登录，生成临时用户，临时用户id：{}", uuid);
            userInfoTo.setUserKey(uuid);
        }

        // 将用户信息存储在ThreadLocal中，供后续处理使用
        threadLocal.set(userInfoTo);
        return true; // 表示继续执行后续的拦截器或处理器
    }

    /**
     * 在处理请求之后执行的逻辑。
     * 主要用于检查当前用户是否为临时用户，并根据结果设置相应的cookie。
     *
     * @param request  客户端的请求对象，用于获取请求信息。
     * @param response 客户端的响应对象，用于设置响应信息，如cookie。
     * @param handler  处理请求的具体对象。
     * @param modelAndView 用于存储视图和模型数据的对象。
     * @throws Exception 抛出异常的处理。
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get(); // 从线程本地存储中获取用户信息
        if (!userInfoTo.getTempUser()) { // 如果当前用户不是临时用户
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey()); // 创建cookie
            cookie.setDomain("gulimall.com"); // 设置cookie的作用域
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT); // 设置cookie的最大生存时间
            response.addCookie(cookie); // 将cookie添加到响应中
        }
    }
}
