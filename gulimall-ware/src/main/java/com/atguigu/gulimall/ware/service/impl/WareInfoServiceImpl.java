package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.ware.dao.WareInfoDao;
import com.atguigu.gulimall.ware.entity.WareInfoEntity;
import com.atguigu.gulimall.ware.service.WareInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    /**
     * 查询仓库信息的分页数据。
     *
     * @param params 查询参数，可以包含关键字(key)用于查询条件构建。
     * @return 返回分页查询结果的工具类，包含当前页数据、总页数、总记录数等信息。
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        // 创建Lambda查询包装器
        LambdaQueryWrapper<WareInfoEntity> wareInfoEntityQueryWrapper = new LambdaQueryWrapper<>();
        String key = (String) params.get("key"); // 获取查询关键字

        // 如果关键字不为空，构建查询条件
        if (!StringUtils.isEmpty(key)) {
            wareInfoEntityQueryWrapper
                    .eq(WareInfoEntity::getId, key).or() // 根据ID等于关键字查询
                    .like(WareInfoEntity::getName, key).or() // 根据仓库名包含关键字查询
                    .like(WareInfoEntity::getAddress, key).or() // 根据仓库地址包含关键字查询
                    .like(WareInfoEntity::getAreacode, key); // 根据区域码包含关键字查询
        }

        // 执行分页查询
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wareInfoEntityQueryWrapper
        );

        // 返回分页查询结果
        return new PageUtils(page);
    }


}