package com.example.collegefreshmanhelper.user.service;

import com.example.collegefreshmanhelper.common.model.PageResult;
import com.example.collegefreshmanhelper.user.dto.UserAvatarUpdateRequest;
import com.example.collegefreshmanhelper.user.dto.UserProfileUpdateRequest;
import com.example.collegefreshmanhelper.user.vo.MyCenterLikeVO;
import com.example.collegefreshmanhelper.user.vo.MyCenterPostVO;
import com.example.collegefreshmanhelper.user.vo.MyCenterReplyVO;
import com.example.collegefreshmanhelper.user.vo.MyCenterSummaryVO;

public interface MyCenterService {

    MyCenterSummaryVO getSummary(Long currentUserId);

    PageResult<MyCenterPostVO> pageMyPosts(Long currentUserId, long pageNum, long pageSize);

    PageResult<MyCenterReplyVO> pageMyReplies(Long currentUserId, long pageNum, long pageSize);

    MyCenterLikeVO getLikeStats(Long currentUserId);

    MyCenterSummaryVO updateProfile(Long currentUserId, UserProfileUpdateRequest request);

    MyCenterSummaryVO updateAvatar(Long currentUserId, UserAvatarUpdateRequest request);
}
