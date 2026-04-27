package com.example.collegefreshmanhelper.forum.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.collegefreshmanhelper.common.model.ApiResponse;
import com.example.collegefreshmanhelper.common.util.LoginUserContext;
import com.example.collegefreshmanhelper.forum.dto.ForumReplyCreateRequest;
import com.example.collegefreshmanhelper.forum.entity.ForumReply;
import com.example.collegefreshmanhelper.forum.service.ForumReplyService;
import com.example.collegefreshmanhelper.forum.vo.ForumReplyThreadVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/replies")
public class ForumReplyController {

    private final ForumReplyService forumReplyService;

    @SaCheckLogin
    @PostMapping
    public ApiResponse<ForumReply> createReply(
            @PathVariable Long postId,
            @Valid @RequestBody ForumReplyCreateRequest request) {
        return ApiResponse.success(forumReplyService.createReply(postId, LoginUserContext.getCurrentUserId(), request));
    }

    @GetMapping
    public ApiResponse<List<ForumReplyThreadVO>> listReplies(@PathVariable Long postId) {
        return ApiResponse.success(forumReplyService.listPublishedRepliesByPostId(postId));
    }
}
