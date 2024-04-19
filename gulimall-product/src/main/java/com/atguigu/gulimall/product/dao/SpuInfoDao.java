package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * spu信息
 *
 * @author Rain^
 * @email 843524258@qq.com
 * @date 2019-10-01 21:08:49
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    @Update("update gulimall_pms.pms_spu_info set publish_status=#{code},update_time=NOW() where id=#{spuId}")
    void updateSpuStatus(@Param("spuId") Long spuId, @Param("code") int code);
}
