package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.BrandDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;


@Service("brandService")
@Slf4j
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 查询品牌分页数据
     * @param params 查询参数，包含页码和每页数量等信息
     * @return 返回分页查询结果，包含当前页数据、总页数、总记录数等信息
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 1、根据参数获取查询关键字
        String key = (String) params.get("key");
        QueryWrapper<BrandEntity> queryWrapper = new QueryWrapper<>();

        // 如果关键字不为空，构建查询条件
        if(!StringUtils.isEmpty(key)){
            queryWrapper.eq("brand_id",key).or().like("name",key);
        }

        // 执行分页查询
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                queryWrapper

        );

        // 返回查询结果
        return new PageUtils(page);
    }


    /**
     * 更新品牌详细信息。
     * 这个方法会更新品牌实体的信息，并且如果品牌名称不为空，会同步更新关联的分类品牌关系及其他相关的数据。
     *
     * @param brand 品牌实体，包含了需要更新的信息。
     */
    @Transactional
    @Override
    public void updateDetail(BrandEntity brand) {
        // 更新品牌实体信息
        log.info("更新品牌信息：{}",brand);
        this.updateById(brand);
        if(!StringUtils.isEmpty(brand.getName())){
            // 如果品牌名称不为空，则进行以下操作
            // 同步更新分类品牌关系表中的品牌名称
            log.info("品牌名称不为空，继续更新分类品牌关系表中的品牌名称");
            categoryBrandRelationService.updateBrand(brand.getBrandId(),brand.getName());

            // TODO: 更新其他可能关联的数据
        }
    }

    @Override
    public List<BrandEntity> getBrandsByIds(List<Long> brandIds) {
        return baseMapper.selectList(new LambdaQueryWrapper<BrandEntity>().in(BrandEntity::getBrandId,brandIds));
    }
}