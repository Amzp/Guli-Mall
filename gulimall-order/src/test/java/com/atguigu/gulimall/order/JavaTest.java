package com.atguigu.gulimall.order;

import org.junit.Test;

import java.util.ArrayList;

/**
 * ClassName: JavaTest
 * Package: com.atguigu.gulimall.order
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/5/3 下午6:14
 * @Version 1.0
 */
public class JavaTest {

    @Test
    public void testArrayList() {
        long startTime = System.currentTimeMillis();
        System.out.println("testArrayList()\n");


        // testArrayList Code
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        for (int i = 2; i <= 10; i++) {
            list.add(i);
        }
        list.add(11);


        System.out.printf("\ntestArrayList  Execution time: %d ms", (System.currentTimeMillis() - startTime));
    }
}
