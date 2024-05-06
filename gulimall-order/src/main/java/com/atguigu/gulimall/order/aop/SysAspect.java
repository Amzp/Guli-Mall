package com.atguigu.gulimall.order.aop;

import com.atguigu.common.annotation.LogInfo;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ClassName: SysAspect
 * Package: com.atguigu.gulimall.order.aop
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/5/2 下午9:07
 * @Version 1.0
 */
@Component
@Aspect
@Slf4j
public class SysAspect {

    /**
     * 定义切面的切入点，扫描到被com.atguigu.gulimall.order.annotation.Log注解标记的任何方法。
     *
     * @Pointcut 指定切面的切入点，此处为方法级别的注解扫描。
     */
    @Pointcut("@annotation(com.atguigu.common.annotation.LogInfo)")
    public void pointCutAnnotation() {
    }

    /**
     * 定义一个切面，指定切面的表达式为执行com.atguigu.gulimall.order包下的所有方法。
     * 这个切面不接受任何参数，也不返回任何值。
     */
    @Pointcut("execution(* com.atguigu.gulimall.order.*.*(..))")
    public void pointcutPackage() {
        // 这个方法体为空，因为@Pointcut注解定义了一个切面，具体的逻辑会在其他地方通过通知（Advice）来执行。
    }

    /**
     * 环绕通知，对匹配"pointCut()"的切面进行增强。
     * 在方法执行前、执行后和发生异常时执行自定义逻辑。
     *
     * @param joinPoint 切面连接点，提供访问被通知对象的方法、参数等信息。
     * @return 返回被通知方法的执行结果。
     * @throws Throwable 如果执行过程中发生异常，则抛出。
     */
    @Around("pointCutAnnotation()")
//    @Around("pointcutPackage()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        LocalDateTime startTime = null;
        Duration executionTime = null;

        // 获取当前执行用户的信息，假设通过解析session或token实现
        // 获取方法签名和实际方法对象
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        // 检查方法是否
        if (method != null ) {
            LogInfo logInfoAnnotation = method.getAnnotation(LogInfo.class);
            log.debug("name：[{}], 方法名：[{}]", logInfoAnnotation.name(), method.getName());

            getHttpRequestInfo();   // 获取和记录当前HTTP请求的相关信息。

            getCurrentOsInfo(); // 获取并打印当前内存信息和操作系统信息。

            startTime = LocalDateTime.now(); // 在执行前记录开始时间
            log.debug("操作开始时间：{}", startTime);   // 记录操作时间
            Object result = joinPoint.proceed();    // 继续执行被通知的方法
            LocalDateTime endTime = LocalDateTime.now();    // 记录操作结束时间
            log.debug("操作结束时间：{}", endTime);

            // 计算并记录执行所需时间
            executionTime = Duration.between(startTime, endTime);
            log.debug("方法[{}]执行耗时：{} 毫秒\n", method.getName(), executionTime.toMillis());

            return result;
        } else {
            return joinPoint.proceed();
        }
    }

    /**
     * 获取和记录当前HTTP请求的相关信息。
     * 该方法不接受参数，也不返回任何值。
     * 主要功能包括：
     * 1. 从RequestContextHolder获取当前请求的RequestAttributes。
     * 2. 将RequestAttributes强制转换为Servlet请求属性，并从中获取HttpServletRequest对象。
     * 3. 如果获取到HttpServletRequest对象，则记录请求的URI、方法类型和发起请求的IP地址。
     */
    private void getHttpRequestInfo() {
        // 获取当前请求的属性
        RequestAttributes reqa = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) reqa;
        HttpServletRequest request = null;
        if (sra != null) {
            request = sra.getRequest();
        }
        // 记录请求信息
        if (request != null) {
            String url = request.getRequestURI();
            String methodName = request.getMethod();
            String ipAddr = getIpAddr(request);
            log.debug("请求的URI：({}), 请求的方法：({}), 请求的IP：({})", url, methodName, ipAddr);
        }
    }


    /**
     * 获取并打印当前内存信息和操作系统信息。
     * 该方法不接受参数，也不返回任何值。
     * 主要功能包括：
     * 1. 获取Java堆内存和非堆内存的使用情况，包括初始化大小、已使用大小、最大大小和已分配大小。
     * 2. 获取操作系统的相关信息，如操作系统名称、架构等。
     * 3. 打印上述内存和操作系统信息到日志中。
     */
    private static void getCurrentOsInfo() {
        // 获取操作系统信息
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

        // 获取当前内存信息
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

        // 解析堆内存使用情况
        long heapInit = heapMemoryUsage.getInit();
        long heapUsed = heapMemoryUsage.getUsed();
        long heapMax = heapMemoryUsage.getMax();
        long heapCommitted = heapMemoryUsage.getCommitted();

        // 解析非堆内存使用情况
        long nonHeapInit = nonHeapMemoryUsage.getInit();
        long nonHeapUsed = nonHeapMemoryUsage.getUsed();
        long nonHeapMax = nonHeapMemoryUsage.getMax();
        long nonHeapCommitted = nonHeapMemoryUsage.getCommitted();

        // 将字节转换为MB
        double heapInitMB = heapInit / (1024.0 * 1024);
        double heapUsedMB = heapUsed / (1024.0 * 1024);
        double heapMaxMB = heapMax / (1024.0 * 1024);
        double heapCommittedMB = heapCommitted / (1024.0 * 1024);

        double nonHeapInitMB = nonHeapInit / (1024.0 * 1024);
        double nonHeapUsedMB = nonHeapUsed / (1024.0 * 1024);
        double nonHeapMaxMB = nonHeapMax / (1024.0 * 1024);
        double nonHeapCommittedMB = nonHeapCommitted / (1024.0 * 1024);


        // 打印堆内存和非堆内存的使用情况
        log.debug("堆内存信息：初始({}MB), 已使用({}MB), 最大({}MB), 已分配({}MB)",
                String.format("%.2f", heapInitMB),
                String.format("%.2f", heapUsedMB),
                String.format("%.2f", heapMaxMB),
                String.format("%.2f", heapCommittedMB));

        log.debug("非堆内存信息：初始({}MB), 已使用({}MB), 最大({}MB), 已分配({}MB)",
                String.format("%.2f", nonHeapInitMB),
                String.format("%.2f", nonHeapUsedMB),
                String.format("%.2f", nonHeapMaxMB),
                String.format("%.2f", nonHeapCommittedMB));


        // 筛选并收集操作系统相关的属性信息
        Map<String, String> systemProperties = System.getProperties().stringPropertyNames().stream()
                .filter(prop -> prop.startsWith("os"))
                .collect(Collectors.toMap(prop -> prop, System::getProperty));

        // 打印操作系统信息和系统负载
        log.debug("操作系统信息：{}", systemProperties);
        double systemLoad = operatingSystemMXBean.getSystemLoadAverage();
        if (systemLoad >= 0) {
            log.debug("系统负载：{}%", String.format("%.2f", systemLoad * 100));
        } else {
            log.warn("无法获取系统负载信息");
        }
    }


    /**
     * 获取客户端的IP地址
     * 此方法首先尝试从HTTP头中获取IP地址，依次检查"x-forwarded-for"、"Proxy-Client-IP"、"WL-Proxy-Client-IP"，
     * 如果这些头都不存在或为空，则退回到获取请求端的IP地址。
     * 注意：此方法适用于通过代理或负载均衡服务器转发请求的场景。
     *
     * @param request 客户端请求对象，用于获取客户端IP地址。
     * @return 客户端的IP地址。如果无法获取到有效的IP地址，则返回本地回环地址"127.0.0.1"。
     */
    public String getIpAddr(HttpServletRequest request) {
        // 尝试从"x-forwarded-for"头获取IP地址，这是常见的代理传递客户端IP的方式
        String ip = request.getHeader("x-forwarded-for");
        // 如果"x-forwarded-for"头不存在或值为空或为"unknown"，则尝试从"Proxy-Client-IP"头获取IP地址
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        // 如果"Proxy-Client-IP"头也不存在或值为空或为"unknown"，则尝试从"WL-Proxy-Client-IP"头获取IP地址
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        // 如果所有尝试都失败，退回到获取请求对象本身的远程地址
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 特殊处理IP为本地回环地址的情况，返回字符串"127.0.0.1"
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

}


