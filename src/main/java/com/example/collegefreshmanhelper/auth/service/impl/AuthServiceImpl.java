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

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final UserStatsService userStatsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser register(RegisterRequest request) {
        validateUsername(request.getUsername());
        validateRole(request.getRole());

        String username = request.getUsername().trim();
        String nickname = request.getNickname().trim();
        if (userService.findByUsername(username) != null) {
            throw new BusinessException("username 账号已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(nickname);
        user.setGender(0);
        user.setRole(request.getRole());
        user.setAdmissionYear(request.getAdmissionYear());
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
        validateUsername(request.getUsername());

        SysUser user = userService.findByUsername(request.getUsername().trim());
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new BusinessException("账号或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("账号或密码错误");
        }

        StpUtil.login(user.getId());
        user.setLastLoginAt(LocalDateTime.now());
        userService.updateById(user);
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

    private void validateUsername(String username) {
        if (username != null && !username.equals(username.trim())) {
            throw new BusinessException("username 账号不能包含前后空格");
        }
    }

    private void validateRole(Integer role) {
        if (!Integer.valueOf(1).equals(role) && !Integer.valueOf(2).equals(role)) {
            throw new BusinessException("role 请选择新生或老生");
        }
    }
}
