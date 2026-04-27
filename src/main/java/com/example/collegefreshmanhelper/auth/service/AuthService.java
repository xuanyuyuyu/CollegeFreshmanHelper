package com.example.collegefreshmanhelper.auth.service;

import com.example.collegefreshmanhelper.auth.dto.LoginRequest;
import com.example.collegefreshmanhelper.auth.dto.RegisterRequest;
import com.example.collegefreshmanhelper.auth.vo.LoginVO;
import com.example.collegefreshmanhelper.user.entity.SysUser;

public interface AuthService {

    SysUser register(RegisterRequest request);

    LoginVO login(LoginRequest request);

    SysUser getCurrentUser();

    void logout();
}
