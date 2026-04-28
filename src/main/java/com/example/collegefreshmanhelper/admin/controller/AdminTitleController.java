package com.example.collegefreshmanhelper.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.example.collegefreshmanhelper.admin.dto.AdminGrantTitleRequest;
import com.example.collegefreshmanhelper.admin.dto.AdminRevokeTitleRequest;
import com.example.collegefreshmanhelper.admin.entity.SysUserTitle;
import com.example.collegefreshmanhelper.admin.service.AdminTitleService;
import com.example.collegefreshmanhelper.admin.vo.AdminTitleVO;
import com.example.collegefreshmanhelper.admin.vo.AdminUserTitleVO;
import com.example.collegefreshmanhelper.common.model.ApiResponse;
import com.example.collegefreshmanhelper.common.model.PageResult;
import com.example.collegefreshmanhelper.common.util.LoginUserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SaCheckRole("admin")
@RequestMapping("/api/admin/titles")
public class AdminTitleController {

    private final AdminTitleService adminTitleService;

    @GetMapping
    public ApiResponse<List<AdminTitleVO>> listTitles() {
        return ApiResponse.success(adminTitleService.listEnabledTitles());
    }

    @GetMapping("/grants/page")
    public ApiResponse<PageResult<AdminUserTitleVO>> pageUserTitles(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long userId) {
        return ApiResponse.success(adminTitleService.pageUserTitles(pageNum, pageSize, userId));
    }

    @PostMapping("/grant")
    public ApiResponse<SysUserTitle> grantTitle(@Valid @RequestBody AdminGrantTitleRequest request) {
        return ApiResponse.success(adminTitleService.grantTitle(LoginUserContext.getCurrentUserId(), request));
    }

    @PostMapping("/grants/{userTitleId}/revoke")
    public ApiResponse<Void> revokeTitle(
            @PathVariable Long userTitleId,
            @RequestBody(required = false) AdminRevokeTitleRequest request) {
        adminTitleService.revokeTitle(
                LoginUserContext.getCurrentUserId(),
                userTitleId,
                request == null ? null : request.getReason()
        );
        return ApiResponse.success(null);
    }
}
