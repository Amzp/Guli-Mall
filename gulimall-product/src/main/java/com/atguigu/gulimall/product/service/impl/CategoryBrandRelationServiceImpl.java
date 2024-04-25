package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.BrandDao;
import com.atguigu.gulimall.product.dao.CategoryBrandRelationDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Resource
    BrandDao brandDao;

    @Resource
    CategoryDao categoryDao;

    @Resource
    CategoryBrandRelationDao relationDao;

    @Resource
    BrandService brandService;

    /**
     * 查询分类品牌关系的分页数据
     *
     * @param params 查询参数，包含分页信息和过滤条件等
     * @return 返回分页查询结果的包装对象，包含总页数、总记录数和当前页的数据列表
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 使用PageHelper进行分页查询，根据传入的参数构建分页信息和查询条件
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        // 将分页查询结果包装成PageUtils对象并返回
        return new PageUtils(page);
    }

    /**
     * 保存品类与品牌关系的详细信息
     *
     * @param categoryBrandRelation 包含品牌ID和品类ID的关系实体
     */
    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();

        // 查询品牌和品类的详细信息
        BrandEntity brandEntity = brandDao.selectById(brandId);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);

        // 设置品类和品牌的名称到关系实体中
        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());

        // 保存更新后的品类品牌关系实体
        this.save(categoryBrandRelation);
    }


    /**
     * 更新品牌信息。
     * @param brandId 品牌的ID，用于确定要更新的品牌。
     * @param name 品牌的新名称，用于更新品牌信息。
     */
    @Override
    public void updateBrand(Long brandId, String name) {
        // 创建一个新的CategoryBrandRelationEntity实例，用于存储更新后的品牌信息
        CategoryBrandRelationEntity relationEntity = new CategoryBrandRelationEntity();
        relationEntity.setBrandId(brandId).setBrandName(name);

        // 使用更新操作，将新的品牌信息应用到数据库中对应的品牌记录
        this.update(relationEntity,
                new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
    }


    /**
     * 更新分类信息
     * @param catId 分类ID，用于指定需要更新的分类
     * @param name 新的分类名称，用于更新指定分类的名称
     */
    @Override
    public void updateCategory(Long catId, String name) {
        // 调用baseMapper的updateCategory方法，更新指定ID的分类名称
        this.baseMapper.updateCategory(catId, name);
    }


    /**
     * 根据分类ID获取关联的品牌列表。
     *
     * @param catId 分类ID，用于查询与之关联的品牌。
     * @return 返回一个品牌实体列表，这些品牌与给定的分类ID有关联。
     */
    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {

        // 通过分类ID查询关联关系列表
        List<CategoryBrandRelationEntity> catelogId = relationDao
                .selectList(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));

        // 遍历关联关系列表，获取每个关联品牌ID，并查询对应的BrandEntity，最后集合返回
        return catelogId.stream()
                .map(item -> brandService.getById(item.getBrandId()))
                .collect(Collectors.toList());
    }


}