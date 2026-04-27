package com.example.collegefreshmanhelper.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.example.collegefreshmanhelper.admin.dto.AdminBanUserRequest;
import com.example.collegefreshmanhelper.admin.service.AdminUserService;
import com.example.collegefreshmanhelper.common.model.ApiResponse;
import com.example.collegefreshmanhelper.common.util.LoginUserContext;
import com.example.collegefreshmanhelper.user.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @SaCheckRole("super-admin")
    @PostMapping("/{userId}/ban")
    public ApiResponse<SysUser> banUser(@PathVariable Long userId, @RequestBody AdminBanUserRequest request) {
        return ApiResponse.success(adminUserService.banUser(LoginUserContext.getCurrentUserId(), userId, request));
    }

    @SaCheckRole("super-admin")
    @PostMapping("/{userId}/unban")
    public ApiResponse<SysUser> unbanUser(@PathVariable Long userId) {
        return ApiResponse.success(adminUserService.unbanUser(LoginUserContext.getCurrentUserId(), userId));
    }
}
