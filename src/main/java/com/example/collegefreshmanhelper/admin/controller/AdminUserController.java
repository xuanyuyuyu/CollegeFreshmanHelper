package com.example.collegefreshmanhelper.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.example.collegefreshmanhelper.admin.dto.AdminBanUserRequest;
import com.example.collegefreshmanhelper.admin.dto.AdminCreateAdminRequest;
import com.example.collegefreshmanhelper.admin.service.AdminUserService;
import com.example.collegefreshmanhelper.admin.vo.AdminUserDetailVO;
import com.example.collegefreshmanhelper.admin.vo.AdminUserSummaryVO;
import com.example.collegefreshmanhelper.common.model.ApiResponse;
import com.example.collegefreshmanhelper.common.model.PageResult;
import com.example.collegefreshmanhelper.common.util.LoginUserContext;
import jakarta.validation.Valid;
import com.example.collegefreshmanhelper.user.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @SaCheckRole("admin")
    @GetMapping("/page")
    public ApiResponse<PageResult<AdminUserSummaryVO>> pageUsers(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(adminUserService.pageUsers(pageNum, pageSize, keyword, role, status));
    }

    @SaCheckRole("admin")
    @GetMapping("/{userId}")
    public ApiResponse<AdminUserDetailVO> detail(@PathVariable Long userId) {
        return ApiResponse.success(adminUserService.getUserDetail(userId));
    }

    @SaCheckRole("super-admin")
    @PostMapping("/admins")
    public ApiResponse<SysUser> createAdmin(@Valid @RequestBody AdminCreateAdminRequest request) {
        return ApiResponse.success(adminUserService.createAdminUser(LoginUserContext.getCurrentUserId(), request));
    }

    @SaCheckRole("super-admin")
    @PostMapping("/{userId}/status")
    public ApiResponse<SysUser> updateAdminStatus(@PathVariable Long userId, @RequestParam Integer status) {
        return ApiResponse.success(adminUserService.updateAdminStatus(LoginUserContext.getCurrentUserId(), userId, status));
    }

    @SaCheckRole("admin")
    @PostMapping("/{userId}/ban")
    public ApiResponse<SysUser> banUser(@PathVariable Long userId, @RequestBody AdminBanUserRequest request) {
        return ApiResponse.success(adminUserService.banUser(LoginUserContext.getCurrentUserId(), userId, request));
    }

    @SaCheckRole("admin")
    @PostMapping("/{userId}/unban")
    public ApiResponse<SysUser> unbanUser(@PathVariable Long userId) {
        return ApiResponse.success(adminUserService.unbanUser(LoginUserContext.getCurrentUserId(), userId));
    }
}
