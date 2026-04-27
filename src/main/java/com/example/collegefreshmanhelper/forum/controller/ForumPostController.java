package com.example.collegefreshmanhelper.forum.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.collegefreshmanhelper.common.model.ApiResponse;
import com.example.collegefreshmanhelper.common.model.PageResult;
import com.example.collegefreshmanhelper.common.util.LoginUserContext;
import com.example.collegefreshmanhelper.forum.dto.ForumPostCreateRequest;
import com.example.collegefreshmanhelper.forum.entity.ForumPost;
import com.example.collegefreshmanhelper.forum.service.ForumPostService;
import com.example.collegefreshmanhelper.forum.vo.ForumPostDetailVO;
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
@RequestMapping("/api/posts")
public class ForumPostController {

    private final ForumPostService forumPostService;

    @SaCheckLogin
    @PostMapping
    public ApiResponse<ForumPost> createPost(@Valid @RequestBody ForumPostCreateRequest request) {
        return ApiResponse.success(forumPostService.createPost(LoginUserContext.getCurrentUserId(), request));
    }

    @GetMapping("/{postId}")
    public ApiResponse<ForumPostDetailVO> getPostDetail(@PathVariable Long postId) {
        return ApiResponse.success(forumPostService.getPostDetail(postId));
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<ForumPost>> pagePublishedPosts(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.success(PageResult.of(forumPostService.pagePublishedPosts(pageNum, pageSize)));
    }
}
