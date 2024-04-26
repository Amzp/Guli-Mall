package com.atguigu.gulimall.auth.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.pojo.GiteeOAuth2Info;
import com.atguigu.gulimall.auth.vo.GiteeUserInfo;
import com.atguigu.gulimall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

import static com.atguigu.common.constant.AuthServerConstant.LOGIN_USER;

/**
 * ClassName: OAuth2Controller
 * Package: com.atguigu.gulimall.auth.controller
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/26 上午8:56
 * @Version 1.0
 */
@Controller
@Slf4j
public class OAuth2Controller {
    @Resource
    private GiteeOAuth2Info giteeOAuth2Info;
    @Resource
    private MemberFeignService memberFeignService;

    // 定义OAuth2授权类型
    private final static String GRANT_TYPE = "grant_type";
    // 定义OAuth2授权代码
    private final static String CODE = "code";
    // 定义客户端ID
    private final static String CLIENT_ID = "client_id";
    // 定义重定向URI
    private final static String REDIRECT_URI = "redirect_uri";
    // 定义客户端密钥
    private final static String CLIENT_SECRET = "client_secret";

    /**
     * 处理Gitee OAuth 2.0登录成功的逻辑。
     *
     * @param code 由Gitee登录服务返回的授权码。
     * @return 返回重定向URL，用于登录成功后的页面跳转。
     * @throws Exception 当HTTP请求或JSON解析发生错误时抛出。
     */
    @GetMapping("/oauth2.0/gitee/success")
    public String giteeLogin(@RequestParam("code") String code, HttpSession session) throws Exception {
        log.debug("开始GiteeOAuth登陆...");

        // 准备OAuth认证所需的参数
        Map<String, String> map = new HashMap<>();
        map.put(GRANT_TYPE, giteeOAuth2Info.getGrantType());
        map.put(CODE, code);
        map.put(CLIENT_ID, giteeOAuth2Info.getClientId());
        map.put(REDIRECT_URI, giteeOAuth2Info.getRedirectUri());
        map.put(CLIENT_SECRET, giteeOAuth2Info.getClientSecret());

        // 1. 根据Code换取accessToken
        HttpResponse response = HttpUtils.doPost("https://gitee.com", "/oauth/token", "post", new HashMap<>(), map, new HashMap<>());

        // 2. 处理换取的accessToken及用户信息
        if (response.getStatusLine().getStatusCode() == 200) {
            log.debug("GiteeOAuth登陆成功...");

            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);

            // 获取Gitee用户UID
            Map<String, String> getQuerys = new HashMap<>();
            getQuerys.put("access_token", socialUser.getAccess_token());
            HttpResponse userInfo = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", new HashMap<>(), getQuerys);
            if (userInfo.getStatusLine().getStatusCode() == 200) {
                GiteeUserInfo giteeUserInfo = JSON.parseObject(EntityUtils.toString(userInfo.getEntity()), GiteeUserInfo.class);
                socialUser.setUid(giteeUserInfo.getId().toString());
                socialUser.setUserName(giteeUserInfo.getName());
            }
            // 调用远程服务进行社交用户登录或注册
            R oauthLogin = memberFeignService.oauthLogin(socialUser);
            if (oauthLogin.getCode() == 0) {
                MemberRespVo data = oauthLogin.getData("data", new TypeReference<MemberRespVo>() {
                });

                log.debug("登陆成功，用户信息：{}", data.toString());

                //1、第一次使用session，命令浏览器保存卡号，JSESSIONID这个cookie
                //以后浏览器访问哪个网站就会带上这个网站的cookie

                session.setAttribute(LOGIN_USER, data);

                // 登录成功，重定向到首页
                return "redirect:http://gulimall.com";
            } else {
                // 登录失败，重定向到登录页面
                return "redirect:http://auth.gulimall.com/login.html";
            }
        } else {
            log.debug("GiteeOAuth登陆失败...");
            // 登录失败，重定向到登录页面
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
