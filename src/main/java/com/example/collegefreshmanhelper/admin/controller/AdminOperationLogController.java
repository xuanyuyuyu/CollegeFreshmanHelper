package com.example.collegefreshmanhelper.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.example.collegefreshmanhelper.admin.service.AdminOperationLogService;
import com.example.collegefreshmanhelper.admin.vo.AdminOperationLogVO;
import com.example.collegefreshmanhelper.common.model.ApiResponse;
import com.example.collegefreshmanhelper.common.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@SaCheckRole("admin")
@RequestMapping("/api/admin/logs")
public class AdminOperationLogController {

    private final AdminOperationLogService adminOperationLogService;

    @GetMapping("/page")
    public ApiResponse<PageResult<AdminOperationLogVO>> pageLogs(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long adminUserId,
            @RequestParam(required = false) Integer targetType,
            @RequestParam(required = false) String operationType) {
        return ApiResponse.success(adminOperationLogService.pageLogs(pageNum, pageSize, adminUserId, targetType, operationType));
    }
}
