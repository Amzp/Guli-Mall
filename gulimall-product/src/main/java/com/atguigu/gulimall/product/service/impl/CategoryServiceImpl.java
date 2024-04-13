package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;

@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    /*，CategoryServiceImpl 类继承自 MyBatis-Plus 提供的 ServiceImpl 抽象类，并通过泛型指定了 CategoryDao 和 CategoryEntity 类型，从而建立了对应的服务与 DAO 和实体类之间的关系。
     *   使用 MyBatis-Plus 的 ServiceImpl 抽象类提供了对数据访问对象（CategoryDao）和实体类（CategoryEntity）的通用 CRUD（增删改查）操作的支持。
     *   ServiceImpl自动注入了CategoryDao 类型的字段
     * */
//    @Autowired
//    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 列出所有分类，并以树形结构进行组织。
     * 这个方法首先查询出所有分类，然后将它们组织成树形结构，其中根节点是一级分类。
     *
     * @return 返回包含所有一级分类的列表，每个一级分类都包含其子分类。
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        // 1、查询出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        // 检查是否有数据
        if (entities.isEmpty()) {
            // 返回空列表
            return new ArrayList<>();
        }

        // 2、将查询结果组装成父子结构的树形列表
        //    2.1、首先筛选出所有一级分类
        List<CategoryEntity> level1Menus = entities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map((menu) -> {
                    // 为每个一级分类查找并设置其子分类
                    menu.setChildren(getChildren(menu, entities));
                    return menu;
                })
                // 按照排序值对一级分类进行排序
                .sorted((menu1, menu2) -> {
                    // 安全获取排序值，如果sort为null，则默认为0
                    Integer sort1 = menu1.getSort() != null ? menu1.getSort() : 0;
                    Integer sort2 = menu2.getSort() != null ? menu2.getSort() : 0;
                    return sort1.compareTo(sort2);
                })
                .collect(Collectors.toList());

        return level1Menus;
    }


    /**
     * 根据菜单ID列表删除菜单。
     * 本方法采用逻辑删除方式，即在数据库中标记删除，而非真正物理删除。
     * 在执行删除前，应检查待删除的菜单是否被其他地方引用，确保删除操作的安全性。
     *
     * @param asList 要删除的菜单ID列表，类型为List<Long>。
     *                需要删除的菜单的ID将以此列表中的元素为依据进行删除操作。
     *                列表为空或null时，方法不执行任何操作。
     */
    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 检查当前删除的菜单，是否被别的地方引用

        // 执行逻辑删除，通过baseMapper的deleteBatchIds方法，根据ID列表批量删除记录
        baseMapper.deleteBatchIds(asList);
    }

    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);


        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     *
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    //225,25,2
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;

    }


    /**
     * 递归查找指定菜单的所有子菜单
     *
     * @param root 当前遍历的菜单项
     * @param all  所有菜单项的列表
     * @return 返回当前菜单项的所有子菜单项列表
     */
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {

        // 1. 使用流处理来查找当前菜单的所有子菜单
        List<CategoryEntity> children = all.stream()
                // 2. 筛选出父菜单ID与当前菜单ID相同的菜单项
                .filter(categoryEntity -> Objects.equals(categoryEntity.getParentCid(), root.getCatId()))
                // 3. 对筛选后的菜单项进行处理，递归找到它们的子菜单
                .map(categoryEntity -> {
                    // 为每个菜单项设置其子菜单
                    categoryEntity.setChildren(getChildren(categoryEntity, all));
                    return categoryEntity;
                })
                // 4. 对菜单项按照排序字段进行排序
                .sorted((menu1, menu2) -> {
                    // 安全获取排序值，如果sort为null，则默认为0
                    Integer sort1 = menu1.getSort() != null ? menu1.getSort() : 0;
                    Integer sort2 = menu2.getSort() != null ? menu2.getSort() : 0;
                    return sort1.compareTo(sort2);
                })
                // 5. 集合化处理后的菜单项列表
                .collect(Collectors.toList());

        return children;
    }


}