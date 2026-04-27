package com.example.collegefreshmanhelper.admin.service.impl;

import com.example.collegefreshmanhelper.admin.dto.AdminBanUserRequest;
import com.example.collegefreshmanhelper.admin.service.AdminOperationLogService;
import com.example.collegefreshmanhelper.admin.service.AdminUserService;
import com.example.collegefreshmanhelper.common.exception.BusinessException;
import com.example.collegefreshmanhelper.user.entity.SysUser;
import com.example.collegefreshmanhelper.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserService userService;
    private final AdminOperationLogService adminOperationLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser banUser(Long adminUserId, Long targetUserId, AdminBanUserRequest request) {
        SysUser user = userService.getVisibleById(targetUserId);
        if (Integer.valueOf(9).equals(user.getRole())) {
            throw new BusinessException("不能封禁超级管理员");
        }

        SysUser before = copyUser(user);
        user.setStatus(0);
        user.setBanReason(request.getReason());
        user.setBanExpireAt(request.getBanExpireAt());
        userService.updateById(user);

        adminOperationLogService.record(adminUserId, 1, targetUserId, "BAN_USER", before, user, request.getReason());
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser unbanUser(Long adminUserId, Long targetUserId) {
        SysUser user = userService.getVisibleById(targetUserId);
        SysUser before = copyUser(user);
        user.setStatus(1);
        user.setBanReason(null);
        user.setBanExpireAt(null);
        userService.updateById(user);

        adminOperationLogService.record(adminUserId, 1, targetUserId, "UNBAN_USER", before, user, null);
        return user;
    }

    private SysUser copyUser(SysUser source) {
        SysUser target = new SysUser();
        target.setId(source.getId());
        target.setUsername(source.getUsername());
        target.setNickname(source.getNickname());
        target.setRole(source.getRole());
        target.setStatus(source.getStatus());
        target.setBanReason(source.getBanReason());
        target.setBanExpireAt(source.getBanExpireAt());
        target.setDeleted(source.getDeleted());
        return target;
    }
}
