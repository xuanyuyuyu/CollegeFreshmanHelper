package com.example.collegefreshmanhelper.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.collegefreshmanhelper.common.model.ApiResponse;
import com.example.collegefreshmanhelper.common.model.PageResult;
import com.example.collegefreshmanhelper.common.util.LoginUserContext;
import com.example.collegefreshmanhelper.user.dto.UserAvatarUpdateRequest;
import com.example.collegefreshmanhelper.user.dto.UserProfileUpdateRequest;
import com.example.collegefreshmanhelper.user.service.MyCenterService;
import com.example.collegefreshmanhelper.user.vo.MyCenterLikeVO;
import com.example.collegefreshmanhelper.user.vo.MyCenterPostVO;
import com.example.collegefreshmanhelper.user.vo.MyCenterReplyVO;
import com.example.collegefreshmanhelper.user.vo.MyCenterSummaryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SaCheckLogin
@RequiredArgsConstructor
@RequestMapping("/api/users/me")
public class MyCenterController {

    private final MyCenterService myCenterService;

    @GetMapping("/summary")
    public ApiResponse<MyCenterSummaryVO> summary() {
        return ApiResponse.success(myCenterService.getSummary(LoginUserContext.getCurrentUserId()));
    }

    @GetMapping("/posts")
    public ApiResponse<PageResult<MyCenterPostVO>> posts(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.success(myCenterService.pageMyPosts(LoginUserContext.getCurrentUserId(), pageNum, pageSize));
    }

    @GetMapping("/replies")
    public ApiResponse<PageResult<MyCenterReplyVO>> replies(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.success(myCenterService.pageMyReplies(LoginUserContext.getCurrentUserId(), pageNum, pageSize));
    }

    @GetMapping("/likes")
    public ApiResponse<MyCenterLikeVO> likes() {
        return ApiResponse.success(myCenterService.getLikeStats(LoginUserContext.getCurrentUserId()));
    }

    @PutMapping("/profile")
    public ApiResponse<MyCenterSummaryVO> updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        return ApiResponse.success(myCenterService.updateProfile(LoginUserContext.getCurrentUserId(), request));
    }

    @PutMapping("/avatar")
    public ApiResponse<MyCenterSummaryVO> updateAvatar(@Valid @RequestBody UserAvatarUpdateRequest request) {
        return ApiResponse.success(myCenterService.updateAvatar(LoginUserContext.getCurrentUserId(), request));
    }
}
