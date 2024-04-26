package com.atguigu.gulimall.auth.pojo;

/**
 * ClassName: GiteeOAuth2Info
 * Package: com.atguigu.gulimall.auth.pojo
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/26 上午9:39
 * @Version 1.0
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * GiteeOAuth2Info类用于存储Gitee OAuth2认证过程中的相关信息。
 * 该类包含了OAuth2认证中需要用到的各参数的声明。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "gitee-oauth2-info")
public class GiteeOAuth2Info {

    // 存储授权类型
    private String grantType;
    // 存储授权代码
    private String code;
    // 存储客户端ID
    private String clientId;
    // 存储重定向URI
    private String redirectUri;
    // 存储客户端密钥
    private String clientSecret;

}
