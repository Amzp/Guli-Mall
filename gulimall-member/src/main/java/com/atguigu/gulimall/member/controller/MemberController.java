package com.atguigu.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.atguigu.gulimall.member.exception.PhoneException;
import com.atguigu.gulimall.member.exception.UsernameException;
import com.atguigu.gulimall.member.feign.CouponFeignService;
import com.atguigu.gulimall.member.vo.MemberUserRegisterVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.common.exception.BizCodeEnume;

import javax.annotation.Resource;


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


    @PostMapping("/register")
    public R register(@RequestBody MemberUserRegisterVo vo) {

//        try {
//            log.debug("开始注册用户信息...");
//            memberService.register(vo);
//        } catch (PhoneException e) {
//            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnum.PHONE_EXIST_EXCEPTION.getMessage());
//        } catch (UsernameException e) {
//            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(),BizCodeEnum.USER_EXIST_EXCEPTION.getMessage());
//        }

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
