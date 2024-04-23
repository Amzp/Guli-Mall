package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


/**
 * 1、引入oss-starter
 * 2、配置key，endpoint相关信息即可
 * 3、使用OSSClient 进行相关操作
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private AttrGroupDao attrGroupDao;

    @Test
    public void testAttrGroupDao(){
        long startTime = System.currentTimeMillis();
        System.out.println("testAttrGroupDao()\n");


        // testAttrGroupDao Code
//        attrGroupDao.selectList(null).forEach(System.out::println);

        attrGroupDao.getAttrGroupWithAttrsBySpuId(100L,225L).forEach(System.out::println);


        System.out.printf("\ntestAttrGroupDao  Execution time: %d ms", (System.currentTimeMillis() - startTime));
    }

    @Test
    public void testRedissonClient(){
        long startTime = System.currentTimeMillis();
        System.out.println("testRedissonClient()\n");


        // testRedissonClient Code
        System.out.println(redissonClient);


        long endTime = System.currentTimeMillis();
        System.out.printf("\ntestRedissonClient  Execution time: %d ms", (endTime - startTime));
    }

    @Test
    public void testRedisTemplate(){
        long startTime = System.currentTimeMillis();
        System.out.println("testRedisTemplate()\n");
        
        
        // testRedisTemplate Code
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("name","zhangsan_"+ UUID.randomUUID());

        String name = ops.get("name");
        System.out.println("name = " + name);

        long endTime = System.currentTimeMillis();
        System.out.printf("\ntestRedisTemplate  Execution time: %d ms", (endTime - startTime));
    }
    

    @Test
    public void testFindPath(){
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路径：{}",Arrays.asList(catelogPath));
    }


    @Test
    public void contextLoads() {

//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setBrandId(1L);
//        brandEntity.setDescript("华为");

//
//        brandEntity.setName("华为");
//        brandService.save(brandEntity);
//        System.out.println("保存成功....");

//        brandService.updateById(brandEntity);


        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
//        list.forEach((item) -> {
//            System.out.println(item);
//        });
        list.forEach(System.out::println);

    }

}
