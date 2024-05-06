package com.atguigu.gulimall.cart.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.constant.CartConstant;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.cart.to.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
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
     * 主要功能包括：判断用户是否已登录，处理登录用户和临时用户的信息，并将用户信息存储在ThreadLocal中，以供后续处理使用。
     *
     * @param request  HttpServletRequest对象，代表客户端的HTTP请求
     * @param response HttpServletResponse对象，用于向客户端发送HTTP响应
     * @param handler  将要执行的目标处理器对象
     * @return boolean 返回值，表示是否继续执行后续的拦截器或处理器。如果返回true，则继续执行，如果返回false，则中断执行。
     * @throws Exception 抛出异常，处理过程中的任何异常都可以抛出。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();

        HttpSession session = request.getSession();
        // 尝试从session中获取已登录的用户信息
        MemberRespVo memberRespVo = (MemberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);

        if (memberRespVo != null) {
            // 用户已登录，处理登录用户信息
            log.info("用户已登录，用户id：{}", memberRespVo.getId());
            userInfoTo.setUserId(memberRespVo.getId());

            threadLocal.set(userInfoTo); // 将用户信息存储在ThreadLocal中
            return true; // 继续执行后续的拦截器或处理器
        }

        // 检查cookie中是否有临时用户信息
        Cookie[] cookies = request.getCookies(); // 获取请求中的所有cookie
        if (cookies != null) {
            for (Cookie cookie : cookies) { // 遍历所有cookie，查找临时用户标识
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

        // 未登录且没有临时用户信息，生成一个新的临时用户ID
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            String uuid = UUID.randomUUID().toString();
            log.info("用户未登录，生成临时用户，临时用户id：{}", uuid);
            userInfoTo.setUserKey(uuid);
        }

        threadLocal.set(userInfoTo); // 将用户信息（可能是登录用户或临时用户）存储在ThreadLocal中
        return true; // 继续执行后续的拦截器或处理器
    }


    /**
     * 在处理请求之后执行的逻辑。
     * 主要用于检查当前用户是否为临时用户，并根据结果设置相应的cookie。
     * 如果用户是临时用户且之前未设置过相关cookie，则本次设置cookie以便于后续识别该临时用户。
     *
     * @param request      客户端的请求对象，用于获取请求信息。
     * @param response     客户端的响应对象，用于设置响应信息，如cookie。
     * @param handler      处理请求的具体对象。
     * @param modelAndView 用于存储视图和模型数据的对象，本方法未使用该参数。
     * @throws Exception 抛出异常的处理。
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 从线程本地存储中获取用户信息
        UserInfoTo userInfoTo = threadLocal.get();
        HttpSession session = request.getSession();
        // 尝试从session中获取已登录的用户信息
        MemberRespVo memberRespVo = (MemberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);

        // 检查当前用户是否为临时用户且未登录
        if (!userInfoTo.getTempUser() && memberRespVo == null) {
            // 创建cookie用于保存临时用户信息
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setDomain("gulimall.com"); // 设置cookie作用域为gulimall.com
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT); // 设置cookie最大生存时间为临时用户cookie的超时时间
            response.addCookie(cookie); // 将cookie添加至响应中
            // 记录日志说明临时用户信息已写入cookie
            log.info("用户未登录，将临时用户信息写入cookie，临时用户id：{}", userInfoTo.getUserKey());
        }
    }

}
