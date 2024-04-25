package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 会员等级
 * 
 * @author Rain^
 * @email 843524258@qq.com
 * @date 2019-10-08 09:47:05
 */
@Mapper
public interface MemberLevelDao extends BaseMapper<MemberLevelEntity> {

    @Select("select * from gulimall_ums.ums_member_level where default_status = 0")
    MemberLevelEntity getDefaultLevel();
}
