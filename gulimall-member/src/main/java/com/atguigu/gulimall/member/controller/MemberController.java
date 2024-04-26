package com.atguigu.gulimall.member.controller;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.exception.PhoneException;
import com.atguigu.gulimall.member.exception.UsernameException;
import com.atguigu.gulimall.member.feign.CouponFeignService;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.gulimall.member.vo.MemberUserLoginVo;
import com.atguigu.gulimall.member.vo.MemberUserRegisterVo;
import com.atguigu.gulimall.member.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;


/**
 * 会员
 *
 * @author Rain^
 * @email 843524258@qq.com
 * @date 2019-10-08 09:47:05
 */
@RestController
@RequestMapping("member/member")
@Slf4j
public class MemberController {
    @Resource
    private MemberService memberService;

    @Resource
    CouponFeignService couponFeignService;


    /**
     * 处理OAuth2登录请求。
     *
     * @param socialUser 包含用户社交登录信息的对象，例如用户名、密码、社交平台标识等。
     * @return R 返回一个结果对象，如果登录成功，包含登录用户的信息；如果登录失败，返回错误信息。
     */
    @PostMapping("/oauth2/login")
    public R oauthLogin(@RequestBody SocialUser socialUser) {
        log.debug("使用提供的社交用户信息进行登录...");

        // 尝试使用提供的社交用户信息进行登录
        MemberEntity memberEntity = memberService.login(socialUser);

        // 判断登录是否成功
        if (memberEntity != null) {
            // 登录成功，返回用户信息
            log.debug("使用提供的社交用户信息登录成功，返回用户信息...");
            return R.ok().setData(memberEntity);
        } else {
            // 登录失败，返回登录错误信息
            log.debug("使用提供的社交用户信息登录失败，返回登录错误信息...");
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getCode(),BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getMsg());
        }
    }



    /**
     * 处理用户登录请求。
     *
     * @param vo 包含登录所需信息的会员用户登录视图对象，例如账号和密码。
     * @return 返回一个结果对象，如果登录成功，返回成功标志；如果登录失败，返回错误信息。
     */
    @PostMapping("/login")
    public R login(@RequestBody MemberUserLoginVo vo) {
        log.debug("开始登录...");

        // 尝试登录，根据提供的账号和密码信息验证用户
        MemberEntity memberEntity = memberService.login(vo);

        // 如果登录失败（用户不存在或密码错误），返回相应的错误信息
        if (memberEntity == null) {
            log.debug("用户名或密码错误...");
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getCode(), BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getMsg());
        }

        // 登录成功，返回成功标志
        log.debug("登录成功...");
        return R.ok().setData(memberEntity);
    }


    /**
     * 用户注册接口
     *
     * @param vo 用户注册信息对象，包含用户名、密码、手机号等信息
     * @return 返回操作结果，成功返回操作成功的标识，失败返回错误信息
     */
    @PostMapping("/register")
    public R register(@RequestBody MemberUserRegisterVo vo) {
        try {
            log.debug("开始注册用户信息...");
            memberService.register(vo);
        } catch (PhoneException e) {
            // 捕获手机号已存在的异常
            log.debug("手机号已存在...");
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        } catch (UsernameException e) {
            // 捕获用户名已存在的异常
            log.debug("用户名已存在...");
            return R.error(BizCodeEnume.USER_EXIST_EXCEPTION.getCode(),BizCodeEnume.USER_EXIST_EXCEPTION.getMsg());
        }

        return R.ok();
    }


    /**
     * <p>处理"/coupons"请求映射，用于测试获取会员（Member）与优惠券（Coupon）相关信息。</p>
     *
     * <h3>功能描述：</h3>
     * <ul>
     *   <li>创建一个具有默认昵称"张三"的MemberEntity实例，代表当前会员。</li>
     *   <li>通过调用couponFeignService的membercoupons()方法远程获取会员的优惠券信息。</li>
     *   <li>构建一个封装了会员信息与优惠券信息的R类型响应对象，并返回给客户端。</li>
     * </ul>
     *
     * <h3>参数说明：</h3>
     * <p>本接口无需外部传入参数。</p>
     *
     * <h3>返回值说明：</h3>
     * <p>返回一个R类型的响应对象，其内部结构如下：</p>
     * <ul>
     *   <li><b>"member"</b>：键为"member"，值为上述创建的MemberEntity实例，包含会员的基本信息，如昵称等。</li>
     *   <li><b>"coupons"</b>：键为"coupons"，值为从couponFeignService.membercoupons()方法获取的优惠券信息列表。
     *     具体优惠券数据结构取决于远程服务接口定义。</li>
     * </ul>
     *
     * @return R 返回一个封装了会员信息与优惠券信息的R类型响应对象。
     */
    @RequestMapping("/coupons")
    public R test() {
        // 创建一个具有默认昵称"张三"的MemberEntity实例，代表当前会员
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");

        // 调用couponFeignService的membercoupons方法远程获取会员的优惠券信息
        R membercoupons = couponFeignService.membercoupons();

        // 构建一个封装了会员信息与优惠券信息的R类型响应对象，并返回给客户端
        return R.ok()
                .put("member", memberEntity)
                .put("coupons", membercoupons.get("coupons"));
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
