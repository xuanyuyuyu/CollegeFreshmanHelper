package com.example.collegefreshmanhelper.admin.service;

import com.example.collegefreshmanhelper.admin.dto.AdminGrantTitleRequest;
import com.example.collegefreshmanhelper.admin.entity.SysUserTitle;
import com.example.collegefreshmanhelper.admin.vo.AdminTitleVO;
import com.example.collegefreshmanhelper.admin.vo.AdminUserTitleVO;
import com.example.collegefreshmanhelper.common.model.PageResult;

import java.util.List;

public interface AdminTitleService {

    List<AdminTitleVO> listEnabledTitles();

    PageResult<AdminUserTitleVO> pageUserTitles(long pageNum, long pageSize, Long userId);

    SysUserTitle grantTitle(Long adminUserId, AdminGrantTitleRequest request);

    void revokeTitle(Long adminUserId, Long userTitleId, String reason);
}
