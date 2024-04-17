package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.to.MemberPrice;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.gulimall.coupon.entity.MemberPriceEntity;
import com.atguigu.gulimall.coupon.entity.SkuLadderEntity;
import com.atguigu.gulimall.coupon.service.MemberPriceService;
import com.atguigu.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.coupon.dao.SkuFullReductionDao;
import com.atguigu.gulimall.coupon.entity.SkuFullReductionEntity;
import com.atguigu.gulimall.coupon.service.SkuFullReductionService;

import javax.annotation.Resource;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Resource
    SkuLadderService skuLadderService;
    @Resource
    MemberPriceService memberPriceService;


    /**
     * 查询SKU满减信息的分页数据
     *
     * @param params 查询参数，封装了当前的分页信息和查询条件
     * @return 返回分页查询结果的工具类，包含当前页的数据和分页信息
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 使用PageHelper进行分页查询，根据传入的参数构造分页信息和查询条件
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        // 将分页查询结果封装成PageUtils工具类并返回
        return new PageUtils(page);
    }

    /**
     * 保存SKU的优惠信息，包括满减、打折和会员价格。
     *
     * @param reductionTo 包含SKU优惠信息的对象，包括skuId、满减条件、折扣信息和会员价格等。
     */
    @Override
    public void saveSkuReduction(SkuReductionTo reductionTo) {
        //1、// //5.4）、sku的优惠、满减等信息；gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
        //sms_sku_ladder
        SkuLadderEntity skuLadderEntity = SkuLadderEntity.builder()
                .skuId(reductionTo.getSkuId())
                .fullCount(reductionTo.getFullCount())
                .discount(reductionTo.getDiscount())
                .addOther(reductionTo.getCountStatus()).build();
        if (reductionTo.getFullCount() > 0) {
            skuLadderService.save(skuLadderEntity);
        }

        //2、保存SKU满减优惠信息：sms_sku_full_reduction
        SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(reductionTo, reductionEntity);
        if (reductionEntity.getFullPrice().compareTo(new BigDecimal("0")) > 0) {
            this.save(reductionEntity);
        }

        //3、保存SKU的会员价格信息：sms_member_price
        List<MemberPrice> memberPrice = reductionTo.getMemberPrice();

        List<MemberPriceEntity> collect = memberPrice.stream()
                .map(item -> MemberPriceEntity.builder()
                        .skuId(reductionTo.getSkuId())
                        .memberLevelId(item.getId())
                        .memberLevelName(item.getName())
                        .memberPrice(item.getPrice())
                        .addOther(1).build())
                .filter(item -> item.getMemberPrice().compareTo(new BigDecimal("0")) > 0)
                .collect(Collectors.toList());

        memberPriceService.saveBatch(collect);
    }

}