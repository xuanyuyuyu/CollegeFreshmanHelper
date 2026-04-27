package com.example.collegefreshmanhelper.auth.service.impl;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.stp.StpUtil;
import com.example.collegefreshmanhelper.auth.dto.LoginRequest;
import com.example.collegefreshmanhelper.auth.dto.RegisterRequest;
import com.example.collegefreshmanhelper.auth.service.AuthService;
import com.example.collegefreshmanhelper.auth.vo.LoginVO;
import com.example.collegefreshmanhelper.common.exception.BusinessException;
import com.example.collegefreshmanhelper.common.util.LoginUserContext;
import com.example.collegefreshmanhelper.user.entity.SysUser;
import com.example.collegefreshmanhelper.user.entity.UserStats;
import com.example.collegefreshmanhelper.user.service.UserService;
import com.example.collegefreshmanhelper.user.service.UserStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final UserStatsService userStatsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser register(RegisterRequest request) {
        if (userService.findByUsername(request.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname().trim());
        user.setGender(0);
        user.setRole(1);
        user.setPoints(0);
        user.setStatus(1);
        user.setDeleted(0);
        userService.save(user);

        UserStats userStats = new UserStats();
        userStats.setUserId(user.getId());
        userStatsService.save(userStats);

        return user;
    }

    @Override
    public LoginVO login(LoginRequest request) {
        SysUser user = userService.findByUsername(request.getUsername());
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }

        StpUtil.login(user.getId());
        return new LoginVO(
                StpUtil.getTokenName(),
                StpUtil.getTokenValue(),
                SaManager.getConfig().getTokenPrefix(),
                user
        );
    }

    @Override
    public SysUser getCurrentUser() {
        return userService.getActiveUserById(LoginUserContext.getCurrentUserId());
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }
}
