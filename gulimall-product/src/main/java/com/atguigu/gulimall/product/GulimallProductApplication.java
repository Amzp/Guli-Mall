package com.atguigu.gulimall.product;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 1、整合MyBatis-Plus
 *      1）、导入依赖
 *      <dependency>
 *             <groupId>com.baomidou</groupId>
 *             <artifactId>mybatis-plus-boot-starter</artifactId>
 *             <version>3.2.0</version>
 *      </dependency>
 *      2）、配置
 *          1、配置数据源；
 *              1）、导入数据库的驱动。https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-versions.html
 *              2）、在application.yml配置数据源相关信息
 *          2、配置MyBatis-Plus；
 *              1）、使用@MapperScan
 *              2）、告诉MyBatis-Plus，sql映射文件位置
 *
 * 2、逻辑删除
 *  1）、配置全局的逻辑删除规则（省略）
 *  2）、配置逻辑删除的组件Bean（省略）
 *  3）、给Bean加上逻辑删除注解@TableLogic
 *
 * 3、JSR303
 *   1）、给Bean添加校验注解:javax.validation.constraints，并定义自己的message提示
 *   2)、开启校验功能@Valid
 *      效果：校验错误以后会有默认的响应；
 *   3）、给校验的bean后紧跟一个BindingResult，就可以获取到校验的结果
 *   4）、分组校验（多场景的复杂校验）
 *         1)、	@NotBlank(message = "品牌名必须提交",groups = {AddGroup.class,UpdateGroup.class})
 *          给校验注解标注什么情况需要进行校验
 *         2）、@Validated({AddGroup.class})
 *         3)、默认没有指定分组的校验注解@NotBlank，在分组校验情况@Validated({AddGroup.class})下不生效，只会在@Validated生效；
 *
 *   5）、自定义校验
 *      1）、编写一个自定义的校验注解
 *      2）、编写一个自定义的校验器 ConstraintValidator
 *      3）、关联自定义的校验器和自定义的校验注解
         *      @Documented
         * @Constraint(validatedBy = { ListValueConstraintValidator.class【可以指定多个不同的校验器，适配不同类型的校验】 })
         * @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
         * @Retention(RUNTIME)
         * public @interface ListValue {
 *
 * 4、统一的异常处理
 * @ControllerAdvice
 *  1）、编写异常处理类，使用@ControllerAdvice。
 *  2）、使用@ExceptionHandler标注方法可以处理的异常。
 */
/**
 * GulimallProductApplication类是商品的Spring Boot启动类。
 *
 * @EnableFeignClients 标注启用Feign客户端，指定扫描的包为"com.atguigu.gulimall.product.feign"，
 *                       用于开启对分布式系统中服务之间调用的支持。
 * @EnableDiscoveryClient 标注启用服务发现客户端，用于加入服务发现框架（如Nacos），以支持微服务架构中的服务自动注册与发现。
 * @MapperScan 指定MyBatis Plus的Mapper接口所在的包，即"com.atguigu.gulimall.product.dao"，
 *              以便Spring自动扫描并注册这些Mapper接口。
 * @SpringBootApplication 标注这是一个Spring Boot应用，并自动包含@SpringBootConfiguration、@EnableAutoConfiguration和@ComponentScan注解，
 *                           实现了Spring Boot应用的快速配置和启动。
 */
@EnableFeignClients(basePackages = "com.atguigu.gulimall.product.feign")
@EnableDiscoveryClient
@MapperScan("com.atguigu.gulimall.product.dao")
@SpringBootApplication
@Slf4j
public class GulimallProductApplication {
    public static void main(String[] args) {
        log.info("开始启动GulimallProductApplication...");
        SpringApplication.run(GulimallProductApplication.class, args);
        log.info("GulimallProductApplication启动成功...");
    }

}
