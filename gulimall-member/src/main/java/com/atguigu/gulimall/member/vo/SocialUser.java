package com.atguigu.gulimall.member.vo;

import lombok.Data;



/**
 * @Description: 社交用户信息
 * @Created: with IntelliJ IDEA.
 * @author: Rain^
 * @createTime: 2020-06-28 11:04
 **/

/**
 * 社交用户类，用于存储Gitee OAuth2.0认证返回的用户信息。
 */
@Data
public class SocialUser {

    // Gitee OAuth2.0认证成功后返回的访问令牌
    private String access_token;
    // 访问令牌的类型
    private String token_type;
    // 访问令牌的有效期，单位为秒
    private Long expires_in;
    // 用于刷新访问令牌的令牌
    private String refresh_token;
    // 访问令牌的权限范围
    private String scope;
    // 访问令牌创建的时间，单位为秒 since Unix Epoch
    private Long created_at;

    // 用户的UID
    private String uid;
    // 用户名
    private String userName;
}
