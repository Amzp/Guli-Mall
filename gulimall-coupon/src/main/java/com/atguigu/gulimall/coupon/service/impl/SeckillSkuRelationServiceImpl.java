package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.dao.SeckillSkuRelationDao;
import com.atguigu.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.atguigu.gulimall.coupon.service.SeckillSkuRelationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

    /**
     * 查询指定条件下的秒杀商品关系页面信息。
     *
     * @param params 包含查询条件的Map，可包含关键字key和促销会话id promotionSessionId。
     * @return 返回页面查询结果，包含分页信息和查询到的数据。
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<SeckillSkuRelationEntity> queryWrapper = new QueryWrapper<>();

        // 根据传入的参数构建查询条件
        String key = (String) params.get("key");
        String promotionSessionId = (String) params.get("promotionSessionId");

        // 如果存在关键字key，则添加到查询条件中
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq("id", key);
        }

        // 如果存在促销会话id，则添加到查询条件中
        if (!StringUtils.isEmpty(promotionSessionId)) {
            queryWrapper.eq("promotion_session_id", promotionSessionId);
        }

        // 执行分页查询
        IPage<SeckillSkuRelationEntity> page = this.page(
                new Query<SeckillSkuRelationEntity>().getPage(params),
                queryWrapper
        );

        // 返回查询结果
        return new PageUtils(page);
    }


}