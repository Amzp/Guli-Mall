package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.ThirdPartFeignService;
import com.atguigu.gulimall.auth.vo.UserRegisterVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ClassName: LoginController
 * Package: com.atguigu.gulimall.auth.controller
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/24 上午11:31
 * @Version 1.0
 */
@Controller
@Slf4j
public class LoginController {
    @Resource
    private ThirdPartFeignService thirdPartFeignService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送验证码
     *
     * @param phone 接收验证码的手机号码
     * @return 返回操作结果，成功返回R.ok()，失败返回错误信息
     */
    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {
        log.debug("开始发送验证码...");
        // 从Redis中获取已存在的验证码
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        // 检查验证码是否在规定时间内被发送过
        if (!StringUtils.isEmpty(redisCode)) {
            long time = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - time < 60 * 1000) {
                // 如果在规定时间内，则返回验证码发送过于频繁的错误信息
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        // 生成新的验证码并设置过期时间
        String sendCode = UUID.randomUUID().toString().substring(0, 6);
        String code = sendCode + "_" + System.currentTimeMillis();
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code, 10, TimeUnit.MINUTES);

        // 调用第三方接口发送验证码
        thirdPartFeignService.sendCode(phone, sendCode);

        // 发送成功，返回操作成功的标识
        return R.ok();
    }


    /**
     * 用户注册处理函数
     * <p>
     * 此函数负责接收前端提交的注册请求，并对用户输入的数据进行校验、验证码验证及远程服务调用，最终完成用户注册流程。
     * 若注册过程中出现错误（如数据验证失败、验证码错误等），将返回至注册页面并携带相应的错误信息。
     *
     * @param vos        用户注册信息验证对象，封装了用户注册时需提交的各项数据（如用户名、密码、手机号、验证码等）
     * @param result     数据绑定与验证结果对象，用于收集用户输入数据的验证结果。若有验证失败的情况，可通过此对象获取具体错误信息。
     * @param attributes 重定向属性对象，用于在重定向时向客户端传递额外数据（如注册失败时的错误信息）。
     * @return 根据注册过程的不同结果，返回对应的重定向URL字符串。成功则重定向至登录页面，失败则重定向至注册页面并附加错误信息。
     */
    @PostMapping("/register")
    public String register(@Valid UserRegisterVo vos, BindingResult result, RedirectAttributes attributes) {
        log.debug("开始注册...");

        // 验证用户提交的注册数据是否符合要求，若有错误则将错误信息保存并返回注册页面
        if (result.hasErrors()) {
            // 收集并整理验证错误信息
            Map<String, String> errors = result.getFieldErrors()
                    .stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

            // 将错误信息添加至重定向属性中，以便在重定向至注册页面时显示给用户
            attributes.addFlashAttribute("errors", errors);

            log.debug("注册信息有误，返回注册页面...");
            return "redirect:http://auth.gulimall.com/reg.html"; // 重定向回注册页面
        }

        // 开始处理验证码验证与用户注册逻辑
        String code = vos.getCode(); // 获取用户提交的验证码

        // 从Redis中获取对应手机号的验证码记录
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vos.getPhone());

        // 验证码有效性判断及后续处理
        if (!StringUtils.isEmpty(redisCode)) {
            // 验证码存在，进行验证码校验
            String[] splitCode = redisCode.split("_"); // 分割验证码以获取有效部分

            // 验证提交的验证码与Redis中存储的验证码是否一致
            if (code.equals(splitCode[0])) {
                // 验证码匹配成功，删除已使用的验证码并调用远程服务进行用户注册
                stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vos.getPhone()); // 删除验证码

                R register = memberFeignService.register(vos); // 调用远程服务进行用户注册

                // 根据注册结果进行后续处理
                if (register.getCode() == 0) {
                    // 注册成功，重定向至登录页面
                    log.debug("注册成功，重定向至登录页面...");
                    return "redirect:http://auth.gulimall.com/login.html";
                } else {
                    // 注册失败，记录错误信息并返回注册页面
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", register.getData("msg", new TypeReference<String>() {
                    })); // 获取注册失败的错误信息
                    attributes.addFlashAttribute("errors", errors); // 将错误信息添加至重定向属性

                    log.debug("注册失败，返回注册页面...");
                    return "redirect:http://auth.gulimall.com/reg.html"; // 重定向回注册页面
                }
            } else {
                // 验证码错误，记录错误信息并返回注册页面
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                attributes.addFlashAttribute("errors", errors); // 将错误信息添加至重定向属性

                log.debug("验证码错误，返回注册页面...");
                return "redirect:http://auth.gulimall.com/reg.html"; // 重定向回注册页面
            }
        } else {
            // 验证码不存在或已过期，记录错误信息并返回注册页面
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            attributes.addFlashAttribute("errors", errors); // 将错误信息添加至重定向属性

            log.debug("验证码错误（未找到或已过期），返回注册页面...");
            return "redirect:http://auth.gulimall.com/reg.html"; // 重定向回注册页面
        }
    }


}