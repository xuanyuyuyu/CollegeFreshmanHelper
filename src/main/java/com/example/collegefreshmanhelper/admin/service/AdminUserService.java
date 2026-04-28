package com.example.collegefreshmanhelper.admin.service;

import com.example.collegefreshmanhelper.admin.vo.AdminUserDetailVO;
import com.example.collegefreshmanhelper.admin.vo.AdminUserSummaryVO;
import com.example.collegefreshmanhelper.common.model.PageResult;
import com.example.collegefreshmanhelper.admin.dto.AdminBanUserRequest;
import com.example.collegefreshmanhelper.admin.dto.AdminCreateAdminRequest;
import com.example.collegefreshmanhelper.user.entity.SysUser;

public interface AdminUserService {

    PageResult<AdminUserSummaryVO> pageUsers(long pageNum, long pageSize, String keyword, Integer role, Integer status);

    AdminUserDetailVO getUserDetail(Long userId);

    SysUser createAdminUser(Long adminUserId, AdminCreateAdminRequest request);

    SysUser updateAdminStatus(Long adminUserId, Long targetUserId, Integer status);

    SysUser banUser(Long adminUserId, Long targetUserId, AdminBanUserRequest request);

    SysUser unbanUser(Long adminUserId, Long targetUserId);
}
