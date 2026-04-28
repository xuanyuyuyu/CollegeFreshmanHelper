package com.example.collegefreshmanhelper.user.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.collegefreshmanhelper.common.model.PageResult;
import com.example.collegefreshmanhelper.forum.entity.ForumPost;
import com.example.collegefreshmanhelper.forum.entity.ForumReply;
import com.example.collegefreshmanhelper.forum.service.ForumPostService;
import com.example.collegefreshmanhelper.forum.service.ForumReplyService;
import com.example.collegefreshmanhelper.user.dto.UserAvatarUpdateRequest;
import com.example.collegefreshmanhelper.user.dto.UserProfileUpdateRequest;
import com.example.collegefreshmanhelper.user.entity.SysUser;
import com.example.collegefreshmanhelper.user.entity.UserStats;
import com.example.collegefreshmanhelper.user.service.MyCenterService;
import com.example.collegefreshmanhelper.user.service.UserService;
import com.example.collegefreshmanhelper.user.service.UserStatsService;
import com.example.collegefreshmanhelper.user.vo.MyCenterLikeVO;
import com.example.collegefreshmanhelper.user.vo.MyCenterPostVO;
import com.example.collegefreshmanhelper.user.vo.MyCenterReplyVO;
import com.example.collegefreshmanhelper.user.vo.MyCenterSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyCenterServiceImpl implements MyCenterService {

    private final UserService userService;
    private final UserStatsService userStatsService;
    private final ForumPostService forumPostService;
    private final ForumReplyService forumReplyService;

    @Override
    public MyCenterSummaryVO getSummary(Long currentUserId) {
        SysUser user = userService.getActiveUserById(currentUserId);
        UserStats stats = getOrInitStats(currentUserId);
        return toSummary(user, stats);
    }

    @Override
    public PageResult<MyCenterPostVO> pageMyPosts(Long currentUserId, long pageNum, long pageSize) {
        userService.getActiveUserById(currentUserId);
        Page<ForumPost> page = forumPostService.lambdaQuery()
                .eq(ForumPost::getUserId, currentUserId)
                .eq(ForumPost::getDeleted, 0)
                .orderByDesc(ForumPost::getCreatedAt)
                .page(new Page<>(pageNum, pageSize));
        Page<MyCenterPostVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(page.getRecords().stream().map(this::toPostVO).toList());
        return PageResult.of(resultPage);
    }

    @Override
    public PageResult<MyCenterReplyVO> pageMyReplies(Long currentUserId, long pageNum, long pageSize) {
        userService.getActiveUserById(currentUserId);
        Page<ForumReply> page = forumReplyService.lambdaQuery()
                .eq(ForumReply::getUserId, currentUserId)
                .eq(ForumReply::getDeleted, 0)
                .orderByDesc(ForumReply::getCreatedAt)
                .page(new Page<>(pageNum, pageSize));
        Map<Long, ForumPost> postMap = loadPostMap(page.getRecords().stream().map(ForumReply::getPostId).filter(Objects::nonNull).collect(Collectors.toSet()));
        Map<Long, SysUser> userMap = loadUserMap(page.getRecords().stream().map(ForumReply::getReplyToUserId).filter(Objects::nonNull).collect(Collectors.toSet()));

        Page<MyCenterReplyVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(page.getRecords().stream().map(reply -> toReplyVO(reply, postMap, userMap)).toList());
        return PageResult.of(resultPage);
    }

    @Override
    public MyCenterLikeVO getLikeStats(Long currentUserId) {
        userService.getActiveUserById(currentUserId);
        UserStats stats = getOrInitStats(currentUserId);
        return toLikeVO(stats);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyCenterSummaryVO updateProfile(Long currentUserId, UserProfileUpdateRequest request) {
        SysUser user = userService.getActiveUserById(currentUserId);
        user.setNickname(request.getNickname().trim());
        user.setGender(request.getGender());
        user.setAdmissionYear(request.getAdmissionYear());
        user.setCollegeName(normalizeText(request.getCollegeName()));
        user.setMajorName(normalizeText(request.getMajorName()));
        user.setBio(normalizeText(request.getBio()));
        userService.updateById(user);
        return getSummary(currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyCenterSummaryVO updateAvatar(Long currentUserId, UserAvatarUpdateRequest request) {
        SysUser user = userService.getActiveUserById(currentUserId);
        user.setAvatarUrl(request.getAvatarUrl().trim());
        userService.updateById(user);
        return getSummary(currentUserId);
    }

    private UserStats getOrInitStats(Long userId) {
        UserStats stats = userStatsService.getById(userId);
        if (stats != null) {
            return fillStatDefaults(stats);
        }
        UserStats initStats = new UserStats();
        initStats.setUserId(userId);
        fillStatDefaults(initStats);
        userStatsService.save(initStats);
        return initStats;
    }

    private UserStats fillStatDefaults(UserStats stats) {
        if (stats.getPostCount() == null) stats.setPostCount(0);
        if (stats.getReplyCount() == null) stats.setReplyCount(0);
        if (stats.getReplyLikeReceivedCount() == null) stats.setReplyLikeReceivedCount(0);
        if (stats.getPostLikeReceivedCount() == null) stats.setPostLikeReceivedCount(0);
        if (stats.getKnowledgeContributionCount() == null) stats.setKnowledgeContributionCount(0);
        if (stats.getFeaturedAnswerCount() == null) stats.setFeaturedAnswerCount(0);
        if (stats.getRewardAppliedCount() == null) stats.setRewardAppliedCount(0);
        if (stats.getRewardPassedCount() == null) stats.setRewardPassedCount(0);
        return stats;
    }

    private MyCenterSummaryVO toSummary(SysUser user, UserStats stats) {
        MyCenterLikeVO likeVO = toLikeVO(stats);
        MyCenterSummaryVO summaryVO = new MyCenterSummaryVO();
        summaryVO.setUserId(user.getId());
        summaryVO.setUsername(user.getUsername());
        summaryVO.setNickname(user.getNickname());
        summaryVO.setAvatarUrl(user.getAvatarUrl());
        summaryVO.setRole(user.getRole());
        summaryVO.setGender(user.getGender());
        summaryVO.setAdmissionYear(user.getAdmissionYear());
        summaryVO.setCollegeName(user.getCollegeName());
        summaryVO.setMajorName(user.getMajorName());
        summaryVO.setBio(user.getBio());
        summaryVO.setPoints(defaultZero(user.getPoints()));
        summaryVO.setStatus(user.getStatus());
        summaryVO.setPostCount(defaultZero(stats.getPostCount()));
        summaryVO.setReplyCount(defaultZero(stats.getReplyCount()));
        summaryVO.setTotalLikeReceivedCount(defaultZero(likeVO.getTotalLikeReceivedCount()));
        summaryVO.setKnowledgeContributionCount(defaultZero(stats.getKnowledgeContributionCount()));
        summaryVO.setFeaturedAnswerCount(defaultZero(stats.getFeaturedAnswerCount()));
        summaryVO.setRewardAppliedCount(defaultZero(stats.getRewardAppliedCount()));
        summaryVO.setRewardPassedCount(defaultZero(stats.getRewardPassedCount()));
        summaryVO.setTitle(likeVO.getTitle());
        summaryVO.setRewardEligible(likeVO.getRewardEligible());
        summaryVO.setRewardHint(likeVO.getRewardHint());
        return summaryVO;
    }

    private MyCenterLikeVO toLikeVO(UserStats stats) {
        int postLikes = defaultZero(stats.getPostLikeReceivedCount());
        int replyLikes = defaultZero(stats.getReplyLikeReceivedCount());
        int totalLikes = postLikes + replyLikes;
        int featuredAnswers = defaultZero(stats.getFeaturedAnswerCount());
        int contributionCount = defaultZero(stats.getKnowledgeContributionCount());
        int replyCount = defaultZero(stats.getReplyCount());

        MyCenterLikeVO likeVO = new MyCenterLikeVO();
        likeVO.setPostLikeReceivedCount(postLikes);
        likeVO.setReplyLikeReceivedCount(replyLikes);
        likeVO.setTotalLikeReceivedCount(totalLikes);
        likeVO.setFeaturedAnswerCount(featuredAnswers);
        likeVO.setKnowledgeContributionCount(contributionCount);
        likeVO.setRewardAppliedCount(defaultZero(stats.getRewardAppliedCount()));
        likeVO.setRewardPassedCount(defaultZero(stats.getRewardPassedCount()));
        likeVO.setTitle(resolveTitle(totalLikes, featuredAnswers, contributionCount, replyCount));
        boolean rewardEligible = totalLikes >= 20 || featuredAnswers >= 2 || contributionCount >= 1 || replyCount >= 15;
        likeVO.setRewardEligible(rewardEligible);
        likeVO.setRewardHint(rewardEligible
                ? "你已经满足阶段性激励条件，可以向管理员申请头衔或物资奖励。"
                : "继续积累高质量回复与获赞，达到条件后可向管理员申请奖励。");
        return likeVO;
    }

    private String resolveTitle(int totalLikes, int featuredAnswers, int contributionCount, int replyCount) {
        if (featuredAnswers >= 5 || totalLikes >= 80) {
            return "高赞答主";
        }
        if (contributionCount >= 3 || featuredAnswers >= 2) {
            return "知识共建者";
        }
        if (replyCount >= 15 || totalLikes >= 20) {
            return "热心学长";
        }
        if (totalLikes >= 5) {
            return "活跃伙伴";
        }
        return "新生伙伴";
    }

    private Map<Long, ForumPost> loadPostMap(Set<Long> postIds) {
        if (postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return forumPostService.listByIds(postIds).stream()
                .collect(Collectors.toMap(ForumPost::getId, Function.identity()));
    }

    private Map<Long, SysUser> loadUserMap(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
    }

    private MyCenterPostVO toPostVO(ForumPost post) {
        MyCenterPostVO postVO = new MyCenterPostVO();
        postVO.setId(post.getId());
        postVO.setTitle(post.getTitle());
        postVO.setContentPreview(stripHtml(post.getContent()));
        postVO.setTags(post.getTags());
        postVO.setStatus(post.getStatus());
        postVO.setVisibility(post.getVisibility());
        postVO.setReplyCount(defaultZero(post.getReplyCount()));
        postVO.setLikeCount(defaultZero(post.getLikeCount()));
        postVO.setPublishedAt(post.getPublishedAt());
        postVO.setCreatedAt(post.getCreatedAt());
        return postVO;
    }

    private MyCenterReplyVO toReplyVO(ForumReply reply, Map<Long, ForumPost> postMap, Map<Long, SysUser> userMap) {
        ForumPost post = postMap.get(reply.getPostId());
        SysUser replyToUser = userMap.get(reply.getReplyToUserId());

        MyCenterReplyVO replyVO = new MyCenterReplyVO();
        replyVO.setId(reply.getId());
        replyVO.setPostId(reply.getPostId());
        replyVO.setPostTitle(post == null ? "原帖已不可见" : post.getTitle());
        replyVO.setParentId(reply.getParentId());
        replyVO.setReplyToReplyId(reply.getReplyToReplyId());
        replyVO.setReplyToUserId(reply.getReplyToUserId());
        replyVO.setReplyToUserNickname(replyToUser == null ? null : replyToUser.getNickname());
        replyVO.setContentPreview(stripHtml(reply.getContent()));
        replyVO.setLikeCount(defaultZero(reply.getLikeCount()));
        replyVO.setChildCount(defaultZero(reply.getChildCount()));
        replyVO.setStatus(reply.getStatus());
        replyVO.setVisibility(reply.getVisibility());
        replyVO.setCreatedAt(reply.getCreatedAt());
        return replyVO;
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private String stripHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
