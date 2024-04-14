package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    /*，CategoryServiceImpl 类继承自 MyBatis-Plus 提供的 ServiceImpl 抽象类，并通过泛型指定了 CategoryDao 和 CategoryEntity 类型，从而建立了对应的服务与 DAO 和实体类之间的关系。
     *   使用 MyBatis-Plus 的 ServiceImpl 抽象类提供了对数据访问对象（CategoryDao）和实体类（CategoryEntity）的通用 CRUD（增删改查）操作的支持。
     *   ServiceImpl自动注入了CategoryDao 类型的字段
     * */
//    @Autowired
//    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 查询分类信息的分页数据。
     *
     * @param params 包含查询参数的Map对象，可以用来指定分页信息和查询条件。
     * @return 返回分类信息的分页工具对象，包含当前页的数据、总页数等信息。
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 使用PageHelper进行分页查询，设置查询条件
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        // 封装分页查询结果到PageUtils工具类中并返回
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

    /**
     * 查找给定目录ID的目录路径。
     * @param catelogId 目录的ID，表示要查找的目录。
     * @return 返回一个Long类型的数组，表示从根目录到指定目录的路径。
     */
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        // 初始化存储路径的列表
        List<Long> paths = new ArrayList<>();

        // 查找父目录路径，并将路径中的每个目录ID添加到paths列表中
        List<Long> parentPath = findParentPath(catelogId, paths);

        // 将父目录路径反转，使其从根目录开始
        Collections.reverse(parentPath);

        // 将列表转换为数组并返回
        return parentPath.toArray(new Long[parentPath.size()]);
    }


    /**
     * 级联更新所有关联的数据
     *
     * 本方法用于当更新一个分类信息时，不仅更新分类本身，同时也会更新与该分类关联的品牌信息。
     *
     * @param category 分类实体对象，包含需要更新的分类信息。
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        // 更新分类本身的信息
        log.info("更新分类本身的信息");
        // this指向CategoryServiceImpl实例，调用的是ServiceImpl基类提供的或本类重写的updateById方法，用于更新分类本身的信息。
        this.updateById(category);

        // 更新与该分类关联的品牌信息
        log.info("更新与该分类关联的品牌信息");
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }


    /**
     * 查找给定分类ID的父级路径
     * @param catelogId 当前分类的ID
     * @param paths 存储已经遍历过的分类ID路径
     * @return 返回包括当前分类ID及其所有父级分类ID的路径列表
     */
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        // 将当前分类ID加入路径列表
        paths.add(catelogId);
        // 通过当前分类ID获取分类实体
        CategoryEntity byId = this.getById(catelogId);
        // 如果当前分类还有父级分类，则递归查找父级分类的路径
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