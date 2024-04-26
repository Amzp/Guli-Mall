package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catalog2Vo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: IndexController
 * Package: com.atguigu.gulimall.product.web
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/19 下午3:51
 * @Version 1.0
 */
@Controller
@Slf4j
public class IndexController {
    @Resource
    private CategoryService categoryService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 处理首页请求，将所有的1级分类信息添加到模型中，并返回首页的逻辑视图名称。
     *
     * @param model 用于在视图中展示数据的模型对象，此处用于存放分类信息。
     * @return 返回逻辑视图名称"index"，对应的实体页面为templates目录下的index.html。
     */
    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        log.info("请求首页");
        // 从服务中查询所有的1级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();

        // 将查询到的分类信息添加到模型中，供视图使用
        model.addAttribute("categorys", categoryEntities);

        // 返回逻辑视图名称，由视图解析器负责将此名称解析为实际的物理视图路径
        return "index";
    }

    /**
     * 获取分类数据的JSON格式
     * <p>
     * 该接口不需要参数，通过GET请求访问，返回一个Map对象，其中键是分类的名称，值是对应分类下的商品目录列表。
     * 这个方法主要用于前端展示商品分类信息，方便用户浏览和选择商品。
     *
     * @return 返回一个Map<String, List < Catalog2Vo>>，其中包含了三级分类的数据结构。
     */
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        log.info("查询三级分类数据");
        // 调用categoryService获取分类数据
        return categoryService.getCatalogJson();
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        // 获取锁
        RLock lock = redissonClient.getLock("my-lock");

        // 加锁
        lock.lock(30, TimeUnit.SECONDS);
        try {
            System.out.println("加锁成功" + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("释放锁" + Thread.currentThread().getId());
            lock.unlock();
        }

        return "hello";
    }

    /**
     * 将生成的唯一标识符字符串写入Redis，并返回该字符串。
     * 该方法不接受任何参数，使用Redis的读写锁来保证并发安全性。
     *
     * @return 返回生成的唯一标识符字符串。
     */
    @ResponseBody
    @GetMapping("/write")
    public String writeValue() {
        // 获取Redis的读写锁
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        String string = "";
        // 获取写锁
        RLock rLock = readWriteLock.writeLock();
        log.info("线程 {} 等待写锁", Thread.currentThread().getId());
        try {
            rLock.lock();
            log.info("线程 {} 获取写锁", Thread.currentThread().getId());
            // 生成一个UUID并转换为字符串
            string = UUID.randomUUID().toString();
            log.info("线程 {} 写入数据 {}", Thread.currentThread().getId(), string);
            // 模拟写入操作的延时，以示例锁的使用场景
            Thread.sleep(30000);
            // 将生成的字符串存储到Redis中
            stringRedisTemplate.opsForValue().set("writeValue", string);
        } catch (InterruptedException e) {
            // 当线程被中断时，抛出运行时异常

            throw new RuntimeException(e);
        } finally {
            // 释放写锁
            rLock.unlock();
            log.info("线程 {} 释放写锁", Thread.currentThread().getId());
        }

        return string;
    }


    /**
     * 从Redis中读取值。
     * 该方法使用Redisson客户端获取一个读锁，然后从Redis模板中读取指定键的值。
     * 注意：该方法不接受任何参数，直接返回从Redis获取的字符串值。
     *
     * @return 返回从Redis中读取到的字符串值。
     */
    @ResponseBody
    @GetMapping("/read")
    public String readValue() {
        // 获取Redis的读写锁
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        // 获取读锁
        RLock rLock = readWriteLock.readLock();
        log.info("线程 {} 等待读锁", Thread.currentThread().getId());
        String s;
        // 加锁，确保并发读取时的数据一致性
        rLock.lock();
        log.info("线程 {} 获取读锁", Thread.currentThread().getId());
        try {
            // 从Redis中读取值
            s = stringRedisTemplate.opsForValue().get("writeValue");
            log.info("线程 {} 读取数据 {}", Thread.currentThread().getId(), s);
            Thread.sleep(30000);
        } catch (Exception e) {
            // 异常处理，将异常转换为运行时异常抛出
            throw new RuntimeException(e);
        } finally {
            // 无论是否发生异常，最后都要释放读锁
            rLock.unlock();
            log.info("线程 {} 释放读锁", Thread.currentThread().getId());
        }
        return s;
    }

    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");
        // 获取一个信号
        boolean b = park.tryAcquire();
        if (b) {
            // 模拟停车时间
            log.info("线程 {} 获取车位", Thread.currentThread().getId());
            return "ok";
        } else {
            log.info("车位已满，请稍后重试");
            return "error";
        }

    }

    @GetMapping("/go")
    @ResponseBody
    public String go() {
        RSemaphore park = redissonClient.getSemaphore("park");
        // 释放一个信号
        park.release();
        log.info("线程 {} 释放车位", Thread.currentThread().getId());
        return "ok";
    }

    @ResponseBody
    @GetMapping("/lockDoor")
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.trySetCount(5);
        // 等待所有线程到达
        log.info("等待所有线程到达");
        door.await();
        log.info("所有线程到达，开始放行");
        return "ok";
    }

    @ResponseBody
    @GetMapping("/go/{id}")
    public String go(@PathVariable("id") Long id) {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        // 计数-1
        door.countDown();
        log.info("{} out", id);
        return id + " out";
    }

}
