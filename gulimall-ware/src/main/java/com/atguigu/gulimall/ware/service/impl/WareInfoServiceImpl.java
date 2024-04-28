package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.dao.WareInfoDao;
import com.atguigu.gulimall.ware.entity.WareInfoEntity;
import com.atguigu.gulimall.ware.feign.MemberFeignService;
import com.atguigu.gulimall.ware.service.WareInfoService;
import com.atguigu.gulimall.ware.vo.FareVo;
import com.atguigu.gulimall.ware.vo.MemberAddressVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {
    @Resource
    private MemberFeignService memberFeignService;

    /**
     * 查询仓库信息的分页数据。
     *
     * @param params 查询参数，可以包含关键字(key)用于查询条件构建。
     * @return 返回分页查询结果的工具类，包含当前页数据、总页数、总记录数等信息。
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        // 创建Lambda查询包装器
        LambdaQueryWrapper<WareInfoEntity> wareInfoEntityQueryWrapper = new LambdaQueryWrapper<>();
        String key = (String) params.get("key"); // 获取查询关键字

        // 如果关键字不为空，构建查询条件
        if (!StringUtils.isEmpty(key)) {
            wareInfoEntityQueryWrapper
                    .eq(WareInfoEntity::getId, key).or() // 根据ID等于关键字查询
                    .like(WareInfoEntity::getName, key).or() // 根据仓库名包含关键字查询
                    .like(WareInfoEntity::getAddress, key).or() // 根据仓库地址包含关键字查询
                    .like(WareInfoEntity::getAreacode, key); // 根据区域码包含关键字查询
        }

        // 执行分页查询
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wareInfoEntityQueryWrapper
        );

        // 返回分页查询结果
        return new PageUtils(page);
    }

    /**
     * 根据地址ID获取运费信息。
     * <p>
     * 通过调用memberFeignService的info方法，获取指定地址ID的详细信息，并根据该地址的电话号码计算运费。
     * 运费计算基于用户手机号码的后两位（倒数第11和12位）。
     * </p>
     *
     * @param addrId 收货地址的ID。
     * @return FareVo 运费信息对象，包括运费和地址详情。如果地址详情为空，则返回null。
     */
    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        // 调用远程服务获取地址详细信息
        R addrInfo = memberFeignService.info(addrId);

        // 将返回的地址信息封装成MemberAddressVo对象
        MemberAddressVo memberAddressVo = addrInfo.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {
        });

        if (memberAddressVo != null) {
            String phone = memberAddressVo.getPhone();
            // 从手机号中截取后两位作为运费计算依据
            String fare = phone.substring(phone.length() - 10, phone.length() - 8);
            BigDecimal bigDecimal = new BigDecimal(fare);

            // 设置运费和地址信息到fareVo对象
            fareVo.setFare(bigDecimal);
            fareVo.setAddress(memberAddressVo);

            return fareVo;
        }
        // 如果地址信息为空，则返回null
        return null;
    }

}