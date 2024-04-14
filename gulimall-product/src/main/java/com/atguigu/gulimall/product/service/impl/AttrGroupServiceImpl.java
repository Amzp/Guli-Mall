package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrService attrService;

    /**
     * 查询属性分组的分页数据
     *
     * @param params 查询参数，包含分页信息和过滤条件
     * @return 返回分页查询结果的包装对象，包含当前页数据、总页数、总记录数等信息
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 使用PageHelper进行分页查询，根据传入的参数构造分页信息和查询条件
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params), new QueryWrapper<>()
        );

        // 将分页查询结果包装成PageUtils对象并返回
        return new PageUtils(page);
    }

    /**
     * 查询指定分类目录下的属性分组页面信息。
     *
     * @param params    查询参数，包括关键字（key）和分类目录ID（catelogId）。
     * @param catelogId 分类目录ID，如果为0，则查询所有分类目录下的属性分组。
     * @return 返回分页查询结果的PageUtils对象，包含当前页的属性分组信息。
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");

        // 构建查询条件，如果关键字不为空，则按照属性分组ID或属性分组名称进行查询
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        // 当key不为空时，为wrapper添加条件，查询条件为attr_group_id等于key或attr_group_name类似key
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key) // 指定attr_group_id等于key
                        .or() // 或者
                        .like("attr_group_name", key); // 指定attr_group_name类似key
            });
        }

        // 根据catelogId是否为0，添加不同的查询条件
        if (catelogId == 0) {
            // 如果catelogId为0，不附加分类目录ID条件进行查询
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        } else {
            // 如果catelogId不为0，附加分类目录ID条件进行查询
            wrapper.eq("catelog_id", catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }
    }


    /**
     * 根据分类id查出所有的分组以及这些组里面的属性
     *
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        //com.atguigu.gulimall.product.vo
        //1、查询分组信息
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        //2、查询所有属性
        List<AttrGroupWithAttrsVo> collect = attrGroupEntities.stream().map(group -> {
            AttrGroupWithAttrsVo attrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(group, attrsVo);
            List<AttrEntity> attrs = attrService.getRelationAttr(attrsVo.getAttrGroupId());
            attrsVo.setAttrs(attrs);
            return attrsVo;
        }).collect(Collectors.toList());

        return collect;


    }

}