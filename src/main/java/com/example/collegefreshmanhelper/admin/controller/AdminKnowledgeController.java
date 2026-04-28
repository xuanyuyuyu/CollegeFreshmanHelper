package com.example.collegefreshmanhelper.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.example.collegefreshmanhelper.admin.dto.AdminKnowledgeCreateRequest;
import com.example.collegefreshmanhelper.admin.dto.AdminKnowledgeStatusUpdateRequest;
import com.example.collegefreshmanhelper.admin.entity.KnowledgeQaTrace;
import com.example.collegefreshmanhelper.admin.service.AdminKnowledgeService;
import com.example.collegefreshmanhelper.admin.vo.AdminKnowledgeVO;
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

@RestController
@RequiredArgsConstructor
@SaCheckRole("admin")
@RequestMapping("/api/admin/knowledge")
public class AdminKnowledgeController {

    private final AdminKnowledgeService adminKnowledgeService;

    @GetMapping("/page")
    public ApiResponse<PageResult<AdminKnowledgeVO>> pageKnowledge(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String category) {
        return ApiResponse.success(adminKnowledgeService.pageKnowledge(pageNum, pageSize, keyword, status, category));
    }

    @PostMapping
    public ApiResponse<KnowledgeQaTrace> createKnowledge(@Valid @RequestBody AdminKnowledgeCreateRequest request) {
        return ApiResponse.success(adminKnowledgeService.createKnowledge(LoginUserContext.getCurrentUserId(), request));
    }

    @PostMapping("/{knowledgeId}/status")
    public ApiResponse<KnowledgeQaTrace> updateStatus(
            @PathVariable Long knowledgeId,
            @Valid @RequestBody AdminKnowledgeStatusUpdateRequest request) {
        return ApiResponse.success(adminKnowledgeService.updateStatus(
                LoginUserContext.getCurrentUserId(), knowledgeId, request.getStatus(), request.getReason()
        ));
    }
}
