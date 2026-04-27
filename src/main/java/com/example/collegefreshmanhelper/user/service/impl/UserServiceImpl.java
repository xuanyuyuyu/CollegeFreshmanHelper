package com.example.collegefreshmanhelper.user.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.collegefreshmanhelper.common.exception.BusinessException;
import com.example.collegefreshmanhelper.user.entity.SysUser;
import com.example.collegefreshmanhelper.user.mapper.SysUserMapper;
import com.example.collegefreshmanhelper.user.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    @Override
    public SysUser getVisibleById(Long id) {
        SysUser user = lambdaQuery()
                .eq(SysUser::getId, id)
                .eq(SysUser::getDeleted, 0)
                .one();
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    @Override
    public SysUser getActiveUserById(Long id) {
        SysUser user = getVisibleById(id);
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }
        if (user.getBanExpireAt() != null && user.getBanExpireAt().isAfter(LocalDateTime.now())) {
            throw new BusinessException("账号已被封禁");
        }
        return user;
    }

    @Override
    public Page<SysUser> pageUsers(long pageNum, long pageSize) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        return lambdaQuery()
                .eq(SysUser::getDeleted, 0)
                .orderByDesc(SysUser::getCreatedAt)
                .page(page);
    }

    @Override
    public SysUser findByUsername(String username) {
        return lambdaQuery()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDeleted, 0)
                .one();
    }
}
