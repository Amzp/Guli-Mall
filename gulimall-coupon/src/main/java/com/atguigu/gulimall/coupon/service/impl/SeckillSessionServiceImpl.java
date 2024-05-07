package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.dao.SeckillSessionDao;
import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;
import com.atguigu.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.atguigu.gulimall.coupon.service.SeckillSessionService;
import com.atguigu.gulimall.coupon.service.SeckillSkuRelationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Resource
    private SeckillSkuRelationService seckillSkuRelationService;

    /**
     * 查询分页的秒杀会话信息。
     *
     * @param params 包含查询参数的Map，其中可能包含关键字(key)用于查询。
     * @return 返回分页查询结果的PageUtils对象，包含当前页的秒杀会话实体列表。
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        // 创建查询包装器
        QueryWrapper<SeckillSessionEntity> queryWrapper = new QueryWrapper<>();

        // 从参数Map中获取关键字
        String key = (String) params.get("key");

        // 如果关键字不为空，添加到查询条件中
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq("id", key);
        }

        // 执行分页查询
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                queryWrapper
        );

        // 返回分页查询结果
        return new PageUtils(page);
    }


    @Override
    public List<SeckillSessionEntity> getLates3DaySession() {

        //计算最近三天
        //查出这三天参与秒杀活动的商品
        List<SeckillSessionEntity> list = this.baseMapper.selectList(new QueryWrapper<SeckillSessionEntity>()
                .between("start_time", startTime(), endTime()));

        if (list != null && !list.isEmpty()) {
            return list.stream()
                    .map(session -> {
                        Long id = session.getId();
                        //查出sms_seckill_sku_relation表中关联的skuId
                        List<SeckillSkuRelationEntity> relationSkus = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>()
                                .eq("promotion_session_id", id));
                        session.setRelationSkus(relationSkus);
                        return session;
                    }).collect(Collectors.toList());
        }

        return null;
    }

    /**
     * 获取当前时间
     *
     * @return 返回当前时间的字符串表示，格式为"yyyy-MM-dd HH:mm:ss"
     */
    private String startTime() {
        // 获取当前日期
        LocalDate now = LocalDate.now();
        // 定义最小时间
        LocalTime min = LocalTime.MIN;
        // 组合当前日期和最小时间得到当天的开始时间
        LocalDateTime start = LocalDateTime.of(now, min);

        // 格式化开始时间
        return start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }


    /**
     * 获取当前时间后两天的最大时间（即两天后当天的23:59:59）
     *
     * @return 格式化后的结束时间字符串，格式为"yyyy-MM-dd HH:mm:ss"
     */
    private String endTime() {
        // 获取当前日期
        LocalDate now = LocalDate.now();
        // 计算结束日期，为当前日期后加两天
        LocalDate plus = now.plusDays(2);
        // 设置结束时间，为一天中的最大时间
        LocalTime max = LocalTime.MAX;
        // 创建结束时间的 LocalDateTime 对象
        LocalDateTime end = LocalDateTime.of(plus, max);

        // 格式化 LocalDateTime 为字符串
        return end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }


    public static void main(String[] args) {
        // LocalDate now = LocalDate.now();
        // LocalDate plus = now.plusDays(2);
        // LocalDateTime now1 = LocalDateTime.now();
        // LocalTime now2 = LocalTime.now();
        //
        // LocalTime max = LocalTime.MAX;
        // LocalTime min = LocalTime.MIN;
        //
        // LocalDateTime start = LocalDateTime.of(now, min);
        // LocalDateTime end = LocalDateTime.of(plus, max);
        //
        // System.out.println(now);
        // System.out.println(now1);
        // System.out.println(now2);
        // System.out.println(plus);
        //
        // System.out.println(start);
        // System.out.println(end);

        // System.out.println(startTime());
        // System.out.println(endTime());
    }

}