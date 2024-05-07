package com.atguigu.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.dao.SeckillSessionDao;
import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;
import com.atguigu.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.atguigu.gulimall.coupon.service.SeckillSessionService;
import com.atguigu.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
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
     * 当前时间
     *
     * @return
     */
    private String startTime() {
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime start = LocalDateTime.of(now, min);

        //格式化时间
        return start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 结束时间
     *
     * @return
     */
    private String endTime() {
        LocalDate now = LocalDate.now();
        LocalDate plus = now.plusDays(2);
        LocalTime max = LocalTime.MAX;
        LocalDateTime end = LocalDateTime.of(plus, max);

        //格式化时间
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