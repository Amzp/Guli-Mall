package com.atguigu.gulimall.thirdparty.controller;


import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;


@RestController
@RequestMapping(value = "/sms")
@Slf4j
public class SmsSendController {

    @Resource
    private JavaMailSender javaMailSender;

    /**
     * 提供给别的微服务进行调用
     *
     * @param phone
     * @param code
     * @return
     */
    @GetMapping(value = "/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        log.debug("短信验证：phone = {}，code = {}", phone, code);

        SimpleMailMessage message = new SimpleMailMessage();

        // 发件人 你的邮箱
        message.setFrom("mengzepeng1998@foxmail.com");
        // 接收人 接收者邮箱
        message.setTo("843524258@qq.com");

        //邮件标题
        message.setSubject("[谷粒商城] 验证码");
//        String authCode = RandomUtil.getSixBitRandom();

        //邮件内容
        String mailContent = "谷粒商城\n" +
                "手机号：" + phone + "\t本次的验证码为:\t " + code + "\t " +
                "\n5 分钟内有效 " +
                "\n时间：" + LocalDateTime.now();
        message.setText(mailContent);

        javaMailSender.send(message);

        log.debug("验证码发送成功，时间：{}", LocalDateTime.now());

        return R.ok();
    }

}
