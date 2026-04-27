package com.example.collegefreshmanhelper.admin.service;

import com.example.collegefreshmanhelper.admin.dto.AdminBanUserRequest;
import com.example.collegefreshmanhelper.user.entity.SysUser;

public interface AdminUserService {

    SysUser banUser(Long adminUserId, Long targetUserId, AdminBanUserRequest request);

    SysUser unbanUser(Long adminUserId, Long targetUserId);
}
