package com.atguigu.gulimall.member;

import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.DigestUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallMemberApplicationTests {

    @Test
    public void contextLoads() {

        String s = DigestUtils.md5DigestAsHex("123456".getBytes());
        System.out.println("s = " + s);

        String s1 = Md5Crypt.md5Crypt("123456".getBytes(), "$1$qqqqqqqq");
        System.out.println("s1 = " + s1);

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        String encode = passwordEncoder.encode("123456");

        System.out.println("encode = " + encode);

        boolean matches = passwordEncoder.matches("123456", "$2a$10$aXrMjmjZ6KBjdxtftIBqteLqWth/AyXk7YTF1b6xaaeceuoqLSfWm");
        System.out.println("matches = " + matches);


    }

}
