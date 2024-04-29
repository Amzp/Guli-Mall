package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.es.SkuEsModel;
import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.dao.SpuInfoDao;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.feign.SearchFeignService;
import com.atguigu.gulimall.product.feign.WareFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


@Service("spuInfoService")
@Slf4j
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Resource
    private SpuInfoDescService spuInfoDescService;
    @Resource
    private SpuImagesService imagesService;
    @Resource
    private AttrService attrService;
    @Resource
    private ProductAttrValueService attrValueService;
    @Resource
    private SkuInfoService skuInfoService;
    @Resource
    private SkuImagesService skuImagesService;
    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Resource
    private BrandService brandService;
    @Resource
    private CategoryService categoryService;

    @Resource
    private CouponFeignService couponFeignService;
    @Resource
    private WareFeignService wareFeignService;
    @Resource
    private SearchFeignService searchFeignService;

    /**
     * 商品上架，更新Spu信息
     *
     * @param spuId 商品的上架ID，用于标识需要上架的商品
     */
    @Override
    public void up(Long spuId) {
        log.info("商品上架，更新Spu信息，spuId = {}", spuId);

        // 查询当前spuId对应的所有sku信息和品牌名字
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuOd(spuId);
        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        // 查询当前sku所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> baseAttrs = attrValueService.baseAttrlistforspu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        List<Long> searchAttrIds = attrService.selectSearchAttrs(attrIds);
        Set<Long> idSet = new HashSet<>(searchAttrIds);

        // 过滤出可搜索的属性，并转换为SkuEsModel.Attrs格式
        List<SkuEsModel.Attrs> attrsList = baseAttrs.stream()
                .filter(item -> idSet.contains(item.getAttrId()))
                .map(item -> {
                    SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(item, attrs1);
                    return attrs1;
                })
                .collect(Collectors.toList());

        // 向库存系统发送远程调用，查询每个SKU的库存情况
        Map<Long, Boolean> stockMap = null;
        try {
            R skuHasStock = wareFeignService.getSkuHasStock(skuIdList);
            // 创建一个TypeReference实例，用于指定泛型列表的类型，其中列表元素类型为SkuHasStockVo。
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>() {
            };


            stockMap = skuHasStock.getData(typeReference).stream()
                    .collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
        } catch (Exception e) {
            log.error("库存服务查询异常，原因 {}", e.toString());
        }

        // 封装每个SKU的信息，包括价格、图片、库存状态等
        Map<Long, Boolean> finalStockMap = stockMap;

        List<SkuEsModel> upProducts = skus.stream()
                .map(sku -> {
                    // 组装需要的数据
                    SkuEsModel esModel = SkuEsModel.builder()
                            .skuPrice(sku.getPrice())
                            .skuImg(sku.getSkuDefaultImg()).build();
                    BeanUtils.copyProperties(sku, esModel);

                    // 设置库存信息
                    if (finalStockMap == null) {
                        esModel.setHasStock(true);
                    } else {
                        esModel.setHasStock(finalStockMap.get(sku.getSkuId()));
                    }

                    // TODO 设置热度评分
                    esModel.setHotScore(0L);

                    // 查询品牌和分类的名字信息，并设置到ES模型中
                    BrandEntity brand = brandService.getById(esModel.getBrandId());
                    esModel.setBrandName(brand.getName()).setBrandImg(brand.getLogo());
                    CategoryEntity category = categoryService.getById(esModel.getCatalogId());
                    esModel.setCatalogName(category.getName());

                    // 设置检索属性
                    esModel.setAttrs(attrsList);

                    return esModel;
                }).collect(Collectors.toList());

        // 将封装好的商品信息保存到ES中
        R r = searchFeignService.productStatusUp(upProducts);
        if (r.getCode() == 0) {
            // 远程调用成功
            // 修改当前spu状态
            this.baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        } else {
            // 远程调用失败
            // TODO 重复调用的问题：接口幂等性、重试机制
        }

    }


    /**
     * 查询SPU信息的分页数据。
     *
     * @param params 查询参数，封装了当前的分页信息和查询条件。
     * @return 返回分页查询结果，包含当前页的数据和分页信息。
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 使用PageHelper进行分页查询，根据传入的参数构造分页信息和查询条件
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        // 将分页查询结果封装成PageUtils返回
        return new PageUtils(page);
    }

    /**
     * 保存SPU（Standard Product Unit）信息
     * TODO 高级篇补充
     *
     * @param vo 包含SPU各种信息的视图对象，例如基本信息、描述、图片、规格参数、积分信息和SKU信息等。
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {

        //1、保存spu基本信息 pms_spu_info
        // 使用LocalDateTime以获得更高的时间精度
        LocalDateTime now = LocalDateTime.now();
        Date currentDate = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        SpuInfoEntity infoEntity = SpuInfoEntity.builder()
                .createTime(currentDate)
                .updateTime(currentDate).build();
        BeanUtils.copyProperties(vo, infoEntity);
        this.saveBaseSpuInfo(infoEntity);

        //2、保存Spu的描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = SpuInfoDescEntity.builder()
                .spuId(infoEntity.getId())
                .decript(String.join(",", decript)).build();
        spuInfoDescService.saveSpuInfoDesc(descEntity);

        //3、保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        imagesService.saveImages(infoEntity.getId(), images);

        //4、保存spu的规格参数;pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream()
                .map(attr -> {
                    AttrEntity id = attrService.getById(attr.getAttrId());
                    return ProductAttrValueEntity.builder()
                            .attrId(attr.getAttrId())
                            .attrName(id.getAttrName())
                            .attrValue(attr.getAttrValues())
                            .quickShow(attr.getShowDesc())
                            .spuId(infoEntity.getId()).build();
                }).collect(Collectors.toList());
        attrValueService.saveProductAttr(collect);

        //5、保存spu的积分信息；gulimall_sms->sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(infoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }

        //5、保存当前spu对应的所有sku信息；
        List<Skus> skus = vo.getSkus();
        if (skus != null && !skus.isEmpty()) {
            skus.forEach(item -> {
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = SkuInfoEntity.builder()
                        .brandId(infoEntity.getBrandId())
                        .catalogId(infoEntity.getCatalogId())
                        .saleCount(0L)
                        .spuId(infoEntity.getId())
                        .skuDefaultImg(defaultImg).build();
                BeanUtils.copyProperties(item, skuInfoEntity);
                //5.1）、sku的基本信息；pms_sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> imagesEntities = item.getImages().stream()
                        .map(img ->
                                SkuImagesEntity.builder()
                                        .skuId(skuId)
                                        .imgUrl(img.getImgUrl())
                                        .defaultImg(img.getDefaultImg()).build())
                        .filter(entity -> {
                            //返回true就是需要，false就是剔除
                            return !StringUtils.isEmpty(entity.getImgUrl());
                        }).collect(Collectors.toList());
                //5.2）、sku的图片信息；pms_sku_image
                skuImagesService.saveBatch(imagesEntities);
                //TODO 没有图片路径的无需保存

                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream()
                        .map(a -> {
                            SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                            BeanUtils.copyProperties(a, attrValueEntity);
                            attrValueEntity.setSkuId(skuId);
                            return attrValueEntity;
                        }).collect(Collectors.toList());
                //5.3）、sku的销售属性信息：pms_sku_sale_attr_value
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // 5.4）、sku的优惠、满减等信息；gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) > 0) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败");
                    }
                }
            });
        }
    }

    /**
     * 保存基础商品信息
     *
     * @param infoEntity 要保存的商品信息实体
     */
    @Override
    public void saveBaseSpuInfo(SpuInfoEntity infoEntity) {
        log.info("保存基础商品信息，将商品信息实体插入数据库...");
        this.baseMapper.insert(infoEntity); // 将商品信息实体插入数据库
    }

    /**
     * 根据条件查询SPU信息分页数据。
     *
     * @param params 查询参数，可包含key（搜索关键字）、status（发布状态）、brandId（品牌ID）、catelogId（分类ID）。
     * @return 返回分页查询结果，包含查询到的数据和分页信息。
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        // 创建查询条件包装器
        LambdaQueryWrapper<SpuInfoEntity> wrapper = new LambdaQueryWrapper<>();

        // 处理关键字搜索逻辑
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq(SpuInfoEntity::getId, key).or().like(SpuInfoEntity::getSpuName, key);
            });
        }

        // 处理发布状态查询逻辑
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq(SpuInfoEntity::getPublishStatus, status);
        }

        // 处理品牌ID查询逻辑，排除无效品牌ID
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq(SpuInfoEntity::getBrandId, brandId);
        }

        // 处理分类ID查询逻辑，排除无效分类ID
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq(SpuInfoEntity::getCatalogId, catelogId);
        }

        // 执行分页查询
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        // 返回查询结果
        return new PageUtils(page);
    }

    /**
     * 通过SKU ID获取对应的SpuInfoEntity信息。
     * @param skuId SKU的ID，用于查询对应的SpuInfoEntity。
     * @return 返回查询到的SpuInfoEntity实例。
     */
    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        // 通过SKU ID获取SKU信息
        SkuInfoEntity infoEntity = skuInfoService.getById(skuId);
        // 从SKU信息中提取出Spu的ID
        Long spuId = infoEntity.getSpuId();
        // 通过提取出的Spu ID获取完整的SpuInfoEntity信息
        return this.getById(spuId);

    }
}