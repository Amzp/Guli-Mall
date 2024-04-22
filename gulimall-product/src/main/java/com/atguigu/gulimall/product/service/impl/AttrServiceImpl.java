package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("attrService")
@Slf4j
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Resource
    AttrAttrgroupRelationDao relationDao;
    @Resource
    AttrGroupDao attrGroupDao;
    @Resource
    CategoryDao categoryDao;

    @Resource
    CategoryService categoryService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存属性信息
     *
     * @param attr 属性信息对象，包含属性的基本信息和关联关系（组ID）
     */
    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        // 1. 使用BeanUtils工具类将attrVo对象的属性值复制到attrEntity对象中
        BeanUtils.copyProperties(attr, attrEntity);

        // 2. 保存属性的基本信息到数据库
        this.save(attrEntity);

        // 3. 判断属性类型，如果是基础属性，并且存在属性组ID，则保存属性与属性组的关联关系
        if ((attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) && (attr.getAttrGroupId() != null)) {
            // 保存属性与属性组的关联关系到数据库
            relationDao.insert(new AttrAttrgroupRelationEntity()
                    // 设置属性组关联关系实体的属性组ID和属性ID
                    .setAttrGroupId(attr.getAttrGroupId())
                    .setAttrId(attrEntity.getAttrId()));
        }
    }

    /**
     * 查询基础属性分页信息
     *
     * @param params    查询参数，包括页码和每页数量等
     * @param catelogId 商品目录ID，用于过滤属性
     * @param type      属性类型，决定是查询基础属性还是销售属性
     * @return 返回属性的分页工具类，包含分页信息和属性列表
     */
    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type) {
        // 构建查询条件，区分基础属性和销售属性
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>()
                .eq("attr_type", "base".equalsIgnoreCase(type) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());

        // 如果目录ID不为0，则添加目录ID的查询条件
        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }

        // 如果存在关键字，则添加关键字查询条件：attr_id等于key或attr_name模糊匹配key
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper
                    .and((wrapper) -> {
                        wrapper.eq("attr_id", key).or().like("attr_name", key);
                    });
        }

        // 执行分页查询
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);

        // 初始化分页工具类并设置查询结果
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        // 处理查询结果，转换为响应对象
        List<AttrRespVo> respVos = records
                .stream()
                .map((attrEntity) -> {
                    AttrRespVo attrRespVo = new AttrRespVo();
                    BeanUtils.copyProperties(attrEntity, attrRespVo);

                    // 设置属性分组名称（仅适用于基础属性）
                    // 判断属性类型是否为"base"，不区分大小写
                    if ("base".equalsIgnoreCase(type)) {
                        // 根据属性ID查询属性与属性组关系实体
                        AttrAttrgroupRelationEntity attrId =
                                relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                        // 检查关系实体是否存在，并且属性组ID不为空
                        if (attrId != null && (attrId.getAttrGroupId() != null)) {
                            // 根据属性组ID查询属性组实体
                            AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrId.getAttrGroupId());
                            // 设置响应体中的属性组名称
                            attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                        }
                    }

                    // 根据属性的目录ID，设置属性所属目录的名称
                    CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
                    if (categoryEntity != null) {
                        // 如果找到了对应的目录实体，则将目录名称设置到属性响应实体中
                        attrRespVo.setCatelogName(categoryEntity.getName());
                    }
                    return attrRespVo;
                })
                .collect(Collectors.toList());

        // 更新分页工具类中的列表为处理后的响应对象列表
        pageUtils.setList(respVos);
        return pageUtils;
    }


    /**
     * 获取属性信息
     *
     * @param attrId 属性ID
     * @return 返回属性的响应体对象，包含属性的详细信息
     */
    @Cacheable(value = {"attr"}, key = "'attrInfo:'+#root.args[0]")
    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrRespVo respVo = new AttrRespVo(); // 创建属性响应体对象
        AttrEntity attrEntity = this.getById(attrId); // 根据属性ID获取属性实体
        BeanUtils.copyProperties(attrEntity, respVo); // 将属性实体的属性值复制到响应体对象

        // 1. 如果属性类型为基础属性，则设置属性分组信息
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // 根据属性ID查询属性与分组关系，设置属性分组ID和分组名称
            AttrAttrgroupRelationEntity attrgroupRelation = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            // 如果属性与分组关系存在，则设置属性分组ID和分组名称
            if (attrgroupRelation != null) {
                respVo.setAttrGroupId(attrgroupRelation.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupRelation.getAttrGroupId());
                if (attrGroupEntity != null) {
                    respVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }

        // 2. 设置属性的分类信息，包括分类路径和分类名称
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId); // 获取分类路径
        respVo.setCatelogPath(catelogPath);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId); // 根据分类ID获取分类实体
        if (categoryEntity != null) {
            respVo.setCatelogName(categoryEntity.getName());
        }

        return respVo;
    }


    /**
     * 更新属性信息
     *
     * @param attr 属性信息对象，包含属性的详细信息
     */
    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        // 将传入的属性VO对象复制到属性实体对象中
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        // 通过ID更新属性实体
        this.updateById(attrEntity);

        // 当属性类型为基本属性时，处理属性与属性组的关联关系
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // 初始化属性与属性组关联实体
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();

            // 设置关联实体的属性组ID和属性ID
            relationEntity.setAttrGroupId(attr.getAttrGroupId()).setAttrId(attr.getAttrId());

            // 查询当前属性ID是否已关联属性组
            Integer count = relationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            // 如果已存在关联，则更新关联信息，否则插入新关联信息
            if (count > 0) {
                relationDao.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            } else {
                relationDao.insert(relationEntity);
            }
        }
    }


    /**
     * 根据分组id查找关联的所有基本属性
     *
     * @param attrgroupId 分组id，用于查找关联属性的依据。
     * @return 返回一个属性实体列表，这些实体与给定的分组id关联。如果没有找到关联的属性，则返回null。
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        // 通过分组id查询所有关联关系实体
        List<AttrAttrgroupRelationEntity> entities =
                relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));

        // 从关联关系实体中提取出所有属性id【注意：collect(Collectors.toList()) 方法在任何情况下都不会返回 null。即使源流为空（即 entities 为空列表），它也会返回一个空的 List<Long>】
        List<Long> attrIds = entities.stream()
                .map(AttrAttrgroupRelationEntity::getAttrId)
                .collect(Collectors.toList());

        // 如果没有提取到属性id，则直接返回null
        if (attrIds.isEmpty()) {
            log.info("没有找到关联的属性");
            return null;
        }
        // 根据提取到的属性id列表查询所有对应的属性实体
        Collection<AttrEntity> attrEntities = this.listByIds(attrIds);
        // 将查询结果转换为List类型并返回
        return (List<AttrEntity>) attrEntities;
    }


    /**
     * 删除属性与属性组的关系。
     *
     * @param vos 属性与属性组关系的视图对象数组，包含需要删除的关系信息。
     *            每个视图对象代表一个属性与属性组的关系。
     *            <p>
     *            注：该方法首先将传入的视图对象数组转换为实体对象列表，然后批量删除这些关系。
     */
    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        // 将视图对象数组转换为实体对象列表
        List<AttrAttrgroupRelationEntity> entities = Arrays.stream(vos)
                .map((item) -> {
                    AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
                    BeanUtils.copyProperties(item, relationEntity); // 使用BeanUtils复制属性
                    return relationEntity;
                })
                .collect(Collectors.toList());

        // 批量删除转换后的实体关系
        relationDao.deleteBatchRelation(entities);
    }


    /**
     * 获取当前分组没有关联的所有属性
     *
     * @param params      请求参数，可包含分页信息和搜索关键字
     * @param attrgroupId 当前属性分组的ID
     * @return 返回属性分页信息，包含未关联的属性列表
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        // 确定当前分组只能关联自己所属的分类中的属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        // 确定当前分组只能关联其他分组未引用的属性
        // 1. 获取当前分类下的其他所有分组
        List<AttrGroupEntity> group = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        // 2. 获取这些分组已关联的属性ID
        List<Long> collect = group.stream()
                .map(AttrGroupEntity::getAttrGroupId)
                .collect(Collectors.toList());

        List<AttrAttrgroupRelationEntity> groupId = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collect));
        List<Long> attrIds = groupId.stream()
                .map(AttrAttrgroupRelationEntity::getAttrId)
                .collect(Collectors.toList());

        // 3. 从当前分类的所有基础属性中排除已关联的属性，得到未关联的属性列表
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (!attrIds.isEmpty()) {
            log.info("需要移除的属性ID：{}", attrIds);
            wrapper.notIn("attr_id", attrIds);
        }
        // 处理搜索条件
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            log.info("搜索条件：{}", key);
            wrapper.and((w) -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        // 执行查询并返回分页结果
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        log.info("返回的未关联的属性列表：Total = {}", page.getTotal());

        return new PageUtils(page);
    }

    /**
     * 在指定的所有属性集合里，挑出检索属性
     *
     * @param attrIds
     * @return
     */
    @Override
    public List<Long> selectSearchAttrs(List<Long> attrIds) {
        return this.baseMapper.selectSearchAttrIds(attrIds);
    }
}