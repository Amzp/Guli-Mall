package com.atguigu.gulimall.product.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SpuImagesDao;
import com.atguigu.gulimall.product.entity.SpuImagesEntity;
import com.atguigu.gulimall.product.service.SpuImagesService;


@Service("spuImagesService")
@Slf4j
public class SpuImagesServiceImpl extends ServiceImpl<SpuImagesDao, SpuImagesEntity> implements SpuImagesService {

    /**
     * 查询SPU图片信息的分页数据。
     *
     * @param params 查询参数，封装了当前的分页信息和查询条件。
     * @return 返回分页查询结果的工具类，包含当前页的数据、总页数、总记录数等信息。
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 使用PageHelper进行分页查询，根据传入的参数构造分页信息和查询条件
        IPage<SpuImagesEntity> page = this.page(
                new Query<SpuImagesEntity>().getPage(params),
                new QueryWrapper<SpuImagesEntity>()
        );

        // 将分页查询结果封装成PageUtils工具类并返回
        return new PageUtils(page);
    }

    /**
     * 保存图片信息到数据库。
     *
     * @param id     商品ID，用于关联图片信息。
     * @param images 图片链接列表，需要保存的图片链接。
     *               <p>该方法首先会检查传入的图片列表是否为空，如果为空则不进行任何操作。
     *               如果图片列表不为空，则将每张图片的信息（商品ID和图片链接）封装为SpuImagesEntity对象，
     *               然后批量保存这些图片信息到数据库。
     */
    @Override
    public void saveImages(Long id, List<String> images) {
        if (images == null || images.isEmpty()) {
            // 如果图片列表为空，则不进行任何操作
            log.info("图片列表为空，不进行任何操作...");
        } else {
            // 将图片链接列表转换为SpuImagesEntity列表，并批量保存到数据库
            log.info("保存图片信息到数据库...");
            List<SpuImagesEntity> collect = images.stream()
                    .map(img -> SpuImagesEntity.builder().spuId(id).imgUrl(img).build())
                    .collect(Collectors.toList());
            this.saveBatch(collect);
            log.info("保存图片信息到数据库成功...");
        }
    }


}