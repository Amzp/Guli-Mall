package com.atguigu.gulimall.member.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.dao.MemberLevelDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.exception.PhoneException;
import com.atguigu.gulimall.member.exception.UsernameException;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.gulimall.member.vo.MemberUserLoginVo;
import com.atguigu.gulimall.member.vo.MemberUserRegisterVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Resource
    private MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );
        return new PageUtils(page);
    }


    /**
     * 注册会员用户。
     *
     * @param vo 包含会员用户注册信息的Vo对象，其中应包括用户名、手机号等必要信息。
     *          Vo对象中应至少包含用户名、手机号和密码。
     */
    @Override
    public void register(MemberUserRegisterVo vo) {
        MemberDao memberDao = this.baseMapper; // 使用BaseMapper来操作会员实体
        MemberEntity memberEntity = new MemberEntity(); // 创建一个新的会员实体对象

        // 设置默认会员等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel(); // 获取默认等级
        memberEntity.setLevelId(levelEntity.getId()); // 将等级ID设置到会员实体中

        // 检查用户名和手机号的唯一性
        checkPhoneUnique(vo.getPhone()); // 检查手机号是否唯一
        checkUsernameUnique(vo.getUserName()); // 检查用户名是否唯一
        memberEntity.setMobile(vo.getPhone()); // 设置手机号
        memberEntity.setUsername(vo.getUserName()); // 设置用户名

        // 密码加密处理
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode); // 加密后的密码存储

        // 其他的默认信息处理，例如积分、成长值等的初始化可以在这里添加

        memberDao.insert(memberEntity); // 将会员实体插入到数据库
    }



    @Override
    public void checkPhoneUnique(String phone) throws PhoneException {
        MemberDao memberDao = this.baseMapper;
        if (memberDao.selectCount(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getMobile, phone)) > 0) {
            throw new PhoneException();
        }
    }

    /**
     * 检查用户名是否唯一
     *
     * @param userName 需要检查的用户名
     * @throws UsernameException 如果用户名已存在，则抛出异常
     */
    @Override
    public void checkUsernameUnique(String userName) throws UsernameException {
        MemberDao memberDao = this.baseMapper; // 使用BaseMapper来操作数据库

        // 查询数据库中与给定用户名相同的记录数量，如果大于0，则表示用户名已存在
        if (memberDao.selectCount(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getUsername, userName)) > 0) {
            throw new UsernameException(); // 抛出用户名不唯一的异常
        }
    }


    /**
     * 用户登录。
     *
     * @param vo 包含登录账号和密码的会员用户登录信息对象
     * @return 如果登录成功，返回对应的会员实体；如果登录失败，返回null
     */
    @Override
    public MemberEntity login(MemberUserLoginVo vo) {
        String loginacct = vo.getLoginacct(); // 获取登录账号
        String password = vo.getPassword(); // 获取密码

        // 通过账号查询会员实体，支持使用用户名或手机号登录
        MemberDao memberDao = this.baseMapper;
        MemberEntity memberEntity = memberDao.selectOne(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getUsername, loginacct).or().eq(MemberEntity::getMobile, loginacct));

        if (memberEntity != null) {
            // 验证密码
            String passwordDb = memberEntity.getPassword(); // 从数据库获取的密码
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // 使用BCrypt加密算法进行密码匹配
            boolean matches = passwordEncoder.matches(password, passwordDb); // 检查密码是否匹配

            if (matches) {
                // 密码匹配成功，返回会员实体
                return memberEntity;
            }
        }

        // 登录失败，返回null
        return null;
    }
}