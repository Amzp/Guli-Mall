package com.atguigu.gulimall.product.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SpuInfoDescDao;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.service.SpuInfoDescService;


@Service("spuInfoDescService")
@Slf4j
public class SpuInfoDescServiceImpl extends ServiceImpl<SpuInfoDescDao, SpuInfoDescEntity> implements SpuInfoDescService {

    /**
     * 查询SPU信息描述的分页数据。
     *
     * @param params 查询参数，封装了当前的分页信息和查询条件。
     * @return 返回分页查询结果，包含当前页的数据和分页信息。
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 使用PageHelper进行分页查询，根据传入的参数获取当前页和查询条件
        IPage<SpuInfoDescEntity> page = this.page(
                new Query<SpuInfoDescEntity>().getPage(params),
                new QueryWrapper<SpuInfoDescEntity>()
        );

        // 将分页查询结果封装成PageUtils返回
        return new PageUtils(page);
    }

    /**
     * 保存SPU信息描述
     * @param descEntity SPU信息描述实体对象
     * 该方法通过调用baseMapper的insert方法，将SPU信息描述实体对象插入到数据库中。
     */
    @Override
    public void saveSpuInfoDesc(SpuInfoDescEntity descEntity) {
        log.info("保存SPU信息描述...");
        this.baseMapper.insert(descEntity);
    }

}