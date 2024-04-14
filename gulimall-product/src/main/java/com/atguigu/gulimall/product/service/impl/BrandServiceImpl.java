package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.BrandDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("brandService")
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


    @Transactional
    @Override
    public void updateDetail(BrandEntity brand) {
        //保证冗余字段的数据一致
        this.updateById(brand);
        if(!StringUtils.isEmpty(brand.getName())){
            //同步更新其他关联表中的数据
            categoryBrandRelationService.updateBrand(brand.getBrandId(),brand.getName());

            //TODO 更新其他关联
        }
    }

}