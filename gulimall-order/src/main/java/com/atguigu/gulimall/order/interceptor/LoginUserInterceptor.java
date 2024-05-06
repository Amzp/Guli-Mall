package com.atguigu.gulimall.order.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.vo.MemberRespVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

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

    /**
     * 请求预处理处理器
     * 作用：在请求到达目标处理器之前进行处理，实现拦截功能。
     *
     * @param request  HttpServletRequest对象，代表客户端的请求
     * @param response HttpServletResponse对象，代表服务器对客户端的响应
     * @param handler  将要处理请求的目标处理器
     * @return boolean 返回值为true表示请求继续向下传递，false表示拦截请求并不再向下传递
     * @throws Exception 抛出异常的处理
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求的URI
        String uri = request.getRequestURI();
        // 使用AntPathMatcher进行路径匹配
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        // 判断请求URI是否匹配特定的路径
        boolean match = antPathMatcher.match("/order/order/status/**", uri);
        boolean match1 = antPathMatcher.match("/payed/notify", uri);
        // "/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**"
        boolean match2 =
                antPathMatcher.match("/swagger-resources/**", uri)
                        || antPathMatcher.match("/webjars/**", uri)
                        || antPathMatcher.match("/v2/**", uri)
                        || antPathMatcher.match("/swagger-ui.html/**", uri)
                        || antPathMatcher.match("/error", uri);
        // 如果匹配，则放行
        if (match || match1 || match2) {
            return true;
        }

        // 尝试从session中获取登录的用户信息
        MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        // 如果用户已登录
        if (attribute != null) {
            // 将用户信息保存在ThreadLocal中，以便在同一个线程中的其他地方访问
            loginUser.set(attribute);
            log.info("拦截器：用户已经登录，用户id为：{}", attribute.getId());
            return true;
        } else {
            // 如果用户未登录，重定向到登录页面，并提示用户登录
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script>alert('请先进行登录，再进行后续操作！');location.href='http://auth.gulimall.com/login.html'</script>");
            log.info("拦截器：用户未登录");
            return false;
        }

    }
}
