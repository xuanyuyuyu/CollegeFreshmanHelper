package com.example.collegefreshmanhelper.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.collegefreshmanhelper.auth.dto.LoginRequest;
import com.example.collegefreshmanhelper.auth.dto.RegisterRequest;
import com.example.collegefreshmanhelper.auth.service.AuthService;
import com.example.collegefreshmanhelper.auth.vo.LoginVO;
import com.example.collegefreshmanhelper.common.model.ApiResponse;
import com.example.collegefreshmanhelper.user.entity.SysUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<SysUser> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @SaCheckLogin
    @GetMapping("/me")
    public ApiResponse<SysUser> me() {
        return ApiResponse.success(authService.getCurrentUser());
    }

    @SaCheckLogin
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.success();
    }
}
