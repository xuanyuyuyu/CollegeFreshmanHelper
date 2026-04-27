package com.example.collegefreshmanhelper.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.example.collegefreshmanhelper.admin.dto.AdminDeleteRequest;
import com.example.collegefreshmanhelper.admin.dto.AdminVisibilityUpdateRequest;
import com.example.collegefreshmanhelper.admin.service.AdminContentService;
import com.example.collegefreshmanhelper.common.model.ApiResponse;
import com.example.collegefreshmanhelper.common.util.LoginUserContext;
import com.example.collegefreshmanhelper.forum.entity.ForumPost;
import com.example.collegefreshmanhelper.forum.entity.ForumReply;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@SaCheckRole("admin")
@RequestMapping("/api/admin")
public class AdminContentController {

    private final AdminContentService adminContentService;

    @PostMapping("/posts/{postId}/visibility")
    public ApiResponse<ForumPost> updatePostVisibility(
            @PathVariable Long postId,
            @Valid @RequestBody AdminVisibilityUpdateRequest request) {
        return ApiResponse.success(adminContentService.updatePostVisibility(
                LoginUserContext.getCurrentUserId(), postId, request.getVisible(), request.getReason()
        ));
    }

    @PostMapping("/posts/{postId}/delete")
    public ApiResponse<ForumPost> deletePost(
            @PathVariable Long postId,
            @RequestBody(required = false) AdminDeleteRequest request) {
        return ApiResponse.success(adminContentService.deletePost(
                LoginUserContext.getCurrentUserId(), postId, request == null ? null : request.getReason()
        ));
    }

    @PostMapping("/replies/{replyId}/visibility")
    public ApiResponse<ForumReply> updateReplyVisibility(
            @PathVariable Long replyId,
            @Valid @RequestBody AdminVisibilityUpdateRequest request) {
        return ApiResponse.success(adminContentService.updateReplyVisibility(
                LoginUserContext.getCurrentUserId(), replyId, request.getVisible(), request.getReason()
        ));
    }

    @PostMapping("/replies/{replyId}/delete")
    public ApiResponse<ForumReply> deleteReply(
            @PathVariable Long replyId,
            @RequestBody(required = false) AdminDeleteRequest request) {
        return ApiResponse.success(adminContentService.deleteReply(
                LoginUserContext.getCurrentUserId(), replyId, request == null ? null : request.getReason()
        ));
    }
}
