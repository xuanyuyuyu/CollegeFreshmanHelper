package com.example.collegefreshmanhelper.user.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.collegefreshmanhelper.common.model.PageResult;
import com.example.collegefreshmanhelper.forum.entity.ForumLikeRecord;
import com.example.collegefreshmanhelper.forum.entity.ForumPost;
import com.example.collegefreshmanhelper.forum.entity.ForumReply;
import com.example.collegefreshmanhelper.forum.mapper.ForumLikeRecordMapper;
import com.example.collegefreshmanhelper.forum.service.ForumPostService;
import com.example.collegefreshmanhelper.forum.service.ForumReplyService;
import com.example.collegefreshmanhelper.user.dto.UserAvatarUpdateRequest;
import com.example.collegefreshmanhelper.user.dto.UserProfileUpdateRequest;
import com.example.collegefreshmanhelper.user.entity.SysUser;
import com.example.collegefreshmanhelper.user.entity.UserStats;
import com.example.collegefreshmanhelper.user.service.MyCenterService;
import com.example.collegefreshmanhelper.user.service.UserService;
import com.example.collegefreshmanhelper.user.service.UserStatsService;
import com.example.collegefreshmanhelper.user.service.UserTitleDisplayService;
import com.example.collegefreshmanhelper.user.service.UserTitleMetrics;
import com.example.collegefreshmanhelper.user.vo.MyCenterLikeVO;
import com.example.collegefreshmanhelper.user.vo.MyCenterLikeDetailVO;
import com.example.collegefreshmanhelper.user.vo.MyCenterLikedItemVO;
import com.example.collegefreshmanhelper.user.vo.MyCenterPostVO;
import com.example.collegefreshmanhelper.user.vo.MyCenterReplyVO;
import com.example.collegefreshmanhelper.user.vo.MyCenterSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
    private final ForumLikeRecordMapper forumLikeRecordMapper;
    private final ForumPostService forumPostService;
    private final ForumReplyService forumReplyService;
    private final UserTitleDisplayService userTitleDisplayService;

    @Override
    public MyCenterSummaryVO getSummary(Long currentUserId) {
        userTitleDisplayService.syncUserTitle(currentUserId);
        SysUser user = userService.getActiveUserById(currentUserId);
        UserStats stats = getOrInitStats(currentUserId);
        return toSummary(user, stats, buildLikeSnapshot(currentUserId, stats));
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
        userTitleDisplayService.syncUserTitle(currentUserId);
        userService.getActiveUserById(currentUserId);
        UserStats stats = getOrInitStats(currentUserId);
        MyCenterLikeVO likeVO = buildLikeSnapshot(currentUserId, stats);
        likeVO.setTitle(resolveDisplayedTitle(currentUserId, likeVO.getTitle()));
        return likeVO;
    }

    @Override
    public PageResult<MyCenterLikeDetailVO> pageMyLikeDetails(Long currentUserId, long pageNum, long pageSize) {
        userService.getActiveUserById(currentUserId);

        List<MyCenterLikeDetailVO> mergedRecords = buildMyLikeDetails(currentUserId);
        int safePageNum = (int) Math.max(pageNum, 1);
        int safePageSize = (int) Math.max(pageSize, 1);
        int fromIndex = Math.min((safePageNum - 1) * safePageSize, mergedRecords.size());
        int toIndex = Math.min(fromIndex + safePageSize, mergedRecords.size());

        return new PageResult<>(
                (long) safePageNum,
                (long) safePageSize,
                (long) mergedRecords.size(),
                mergedRecords.subList(fromIndex, toIndex)
        );
    }

    @Override
    public PageResult<MyCenterLikedItemVO> pageMyLikedItems(Long currentUserId, long pageNum, long pageSize) {
        userService.getActiveUserById(currentUserId);

        Page<ForumLikeRecord> page = new Page<>(pageNum, pageSize);
        Page<ForumLikeRecord> recordPage = forumLikeRecordMapper.selectPage(page,
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ForumLikeRecord>()
                        .eq(ForumLikeRecord::getUserId, currentUserId)
                        .eq(ForumLikeRecord::getLikeStatus, ForumLikeRecord.STATUS_LIKED)
                        .eq(ForumLikeRecord::getDeleted, 0)
                        .orderByDesc(ForumLikeRecord::getLikedAt)
                        .orderByDesc(ForumLikeRecord::getUpdatedAt));

        Set<Long> postTargetIds = recordPage.getRecords().stream()
                .filter(record -> Integer.valueOf(ForumLikeRecord.TARGET_TYPE_POST).equals(record.getTargetType()))
                .map(ForumLikeRecord::getTargetId)
                .collect(Collectors.toSet());
        Set<Long> replyTargetIds = recordPage.getRecords().stream()
                .filter(record -> Integer.valueOf(ForumLikeRecord.TARGET_TYPE_REPLY).equals(record.getTargetType()))
                .map(ForumLikeRecord::getTargetId)
                .collect(Collectors.toSet());

        Map<Long, ForumPost> likedPostMap = loadPostMap(postTargetIds);
        Map<Long, ForumReply> likedReplyMap = loadReplyMap(replyTargetIds);
        Set<Long> replyPostIds = likedReplyMap.values().stream()
                .map(ForumReply::getPostId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, ForumPost> replyPostMap = loadPostMap(replyPostIds);

        Page<MyCenterLikedItemVO> resultPage = new Page<>(recordPage.getCurrent(), recordPage.getSize(), recordPage.getTotal());
        resultPage.setRecords(recordPage.getRecords().stream()
                .map(record -> toLikedItemVO(record, likedPostMap, likedReplyMap, replyPostMap))
                .toList());
        return PageResult.of(resultPage);
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
        return stats;
    }

    private MyCenterSummaryVO toSummary(SysUser user, UserStats stats, MyCenterLikeVO likeVO) {
        int postCount = countMyPosts(user.getId());
        int replyCount = countMyReplies(user.getId());
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
        summaryVO.setPostCount(postCount);
        summaryVO.setReplyCount(replyCount);
        summaryVO.setTotalLikeReceivedCount(defaultZero(likeVO.getTotalLikeReceivedCount()));
        summaryVO.setKnowledgeContributionCount(defaultZero(stats.getKnowledgeContributionCount()));
        summaryVO.setFeaturedAnswerCount(defaultZero(stats.getFeaturedAnswerCount()));
        summaryVO.setTitle(resolveDisplayedTitle(user.getId(), likeVO.getTitle()));
        summaryVO.setTitleRules(userTitleDisplayService.listAutomaticTitleRules());
        return summaryVO;
    }

    private MyCenterLikeVO buildLikeSnapshot(Long userId, UserStats stats) {
        int postLikes = sumMyPostLikes(userId);
        int replyLikes = sumMyReplyLikes(userId);
        int totalLikes = postLikes + replyLikes;
        int featuredAnswers = defaultZero(stats.getFeaturedAnswerCount());
        int contributionCount = defaultZero(stats.getKnowledgeContributionCount());
        int replyCount = countMyReplies(userId);
        UserTitleMetrics titleMetrics = userTitleDisplayService.buildMetrics(stats, replyCount, replyLikes);

        MyCenterLikeVO likeVO = new MyCenterLikeVO();
        likeVO.setPostLikeReceivedCount(postLikes);
        likeVO.setReplyLikeReceivedCount(replyLikes);
        likeVO.setTotalLikeReceivedCount(totalLikes);
        likeVO.setFeaturedAnswerCount(featuredAnswers);
        likeVO.setKnowledgeContributionCount(contributionCount);
        likeVO.setTitle(userTitleDisplayService.resolveAutomaticTitle(titleMetrics));
        return likeVO;
    }

    private String resolveDisplayedTitle(Long userId, String fallbackTitle) {
        String wearingTitle = userTitleDisplayService.findWearingTitle(userId);
        return wearingTitle != null ? wearingTitle : fallbackTitle;
    }

    private int countMyPosts(Long userId) {
        Long count = forumPostService.lambdaQuery()
                .eq(ForumPost::getUserId, userId)
                .eq(ForumPost::getDeleted, 0)
                .count();
        return count == null ? 0 : count.intValue();
    }

    private int countMyReplies(Long userId) {
        Long count = forumReplyService.lambdaQuery()
                .eq(ForumReply::getUserId, userId)
                .eq(ForumReply::getDeleted, 0)
                .count();
        return count == null ? 0 : count.intValue();
    }

    private int sumMyPostLikes(Long userId) {
        return forumPostService.lambdaQuery()
                .eq(ForumPost::getUserId, userId)
                .eq(ForumPost::getDeleted, 0)
                .list()
                .stream()
                .mapToInt(post -> defaultZero(post.getLikeCount()))
                .sum();
    }

    private int sumMyReplyLikes(Long userId) {
        return forumReplyService.lambdaQuery()
                .eq(ForumReply::getUserId, userId)
                .eq(ForumReply::getDeleted, 0)
                .list()
                .stream()
                .mapToInt(reply -> defaultZero(reply.getLikeCount()))
                .sum();
    }

    private List<MyCenterLikeDetailVO> buildMyLikeDetails(Long userId) {
        List<MyCenterLikeDetailVO> postDetails = forumPostService.lambdaQuery()
                .eq(ForumPost::getUserId, userId)
                .eq(ForumPost::getDeleted, 0)
                .gt(ForumPost::getLikeCount, 0)
                .list()
                .stream()
                .map(this::toPostLikeDetailVO)
                .toList();

        Map<Long, ForumPost> postMap = loadPostMap(
                forumReplyService.lambdaQuery()
                        .select(ForumReply::getPostId)
                        .eq(ForumReply::getUserId, userId)
                        .eq(ForumReply::getDeleted, 0)
                        .gt(ForumReply::getLikeCount, 0)
                        .list()
                        .stream()
                        .map(ForumReply::getPostId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())
        );

        List<MyCenterLikeDetailVO> replyDetails = forumReplyService.lambdaQuery()
                .eq(ForumReply::getUserId, userId)
                .eq(ForumReply::getDeleted, 0)
                .gt(ForumReply::getLikeCount, 0)
                .list()
                .stream()
                .map(reply -> toReplyLikeDetailVO(reply, postMap))
                .toList();

        return java.util.stream.Stream.concat(postDetails.stream(), replyDetails.stream())
                .sorted(Comparator
                        .comparing(MyCenterLikeDetailVO::getLikeCount, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(MyCenterLikeDetailVO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private Map<Long, ForumPost> loadPostMap(Set<Long> postIds) {
        if (postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return forumPostService.listByIds(postIds).stream()
                .collect(Collectors.toMap(ForumPost::getId, Function.identity()));
    }

    private Map<Long, ForumReply> loadReplyMap(Set<Long> replyIds) {
        if (replyIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return forumReplyService.listByIds(replyIds).stream()
                .collect(Collectors.toMap(ForumReply::getId, Function.identity()));
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

    private MyCenterLikeDetailVO toPostLikeDetailVO(ForumPost post) {
        MyCenterLikeDetailVO detailVO = new MyCenterLikeDetailVO();
        detailVO.setTargetType("POST");
        detailVO.setTargetId(post.getId());
        detailVO.setPostId(post.getId());
        detailVO.setPostTitle(post.getTitle());
        detailVO.setTargetTitle(post.getTitle());
        detailVO.setContentPreview(stripHtml(post.getContent()));
        detailVO.setLikeCount(defaultZero(post.getLikeCount()));
        detailVO.setStatus(post.getStatus());
        detailVO.setVisibility(post.getVisibility());
        detailVO.setCreatedAt(post.getPublishedAt() == null ? post.getCreatedAt() : post.getPublishedAt());
        return detailVO;
    }

    private MyCenterLikeDetailVO toReplyLikeDetailVO(ForumReply reply, Map<Long, ForumPost> postMap) {
        ForumPost post = postMap.get(reply.getPostId());
        MyCenterLikeDetailVO detailVO = new MyCenterLikeDetailVO();
        detailVO.setTargetType("REPLY");
        detailVO.setTargetId(reply.getId());
        detailVO.setPostId(reply.getPostId());
        detailVO.setPostTitle(post == null ? "原帖已不可见" : post.getTitle());
        detailVO.setTargetTitle(post == null ? "我的回复" : "回复于《" + post.getTitle() + "》");
        detailVO.setContentPreview(stripHtml(reply.getContent()));
        detailVO.setLikeCount(defaultZero(reply.getLikeCount()));
        detailVO.setStatus(reply.getStatus());
        detailVO.setVisibility(reply.getVisibility());
        detailVO.setCreatedAt(reply.getCreatedAt());
        return detailVO;
    }

    private MyCenterLikedItemVO toLikedItemVO(
            ForumLikeRecord record,
            Map<Long, ForumPost> likedPostMap,
            Map<Long, ForumReply> likedReplyMap,
            Map<Long, ForumPost> replyPostMap) {
        MyCenterLikedItemVO itemVO = new MyCenterLikedItemVO();
        itemVO.setTargetId(record.getTargetId());
        itemVO.setLikedAt(record.getLikedAt());

        if (Integer.valueOf(ForumLikeRecord.TARGET_TYPE_POST).equals(record.getTargetType())) {
            ForumPost post = likedPostMap.get(record.getTargetId());
            itemVO.setTargetType("POST");
            itemVO.setPostId(record.getTargetId());
            itemVO.setPostTitle(post == null ? "原帖已不可见" : post.getTitle());
            itemVO.setTargetTitle(post == null ? "帖子已不可见" : post.getTitle());
            itemVO.setContentPreview(post == null ? "原帖已不可见" : stripHtml(post.getContent()));
            itemVO.setLikeCount(post == null ? 0 : defaultZero(post.getLikeCount()));
            itemVO.setStatus(post == null ? 0 : post.getStatus());
            itemVO.setVisibility(post == null ? 0 : post.getVisibility());
            return itemVO;
        }

        ForumReply reply = likedReplyMap.get(record.getTargetId());
        ForumPost post = reply == null ? null : replyPostMap.get(reply.getPostId());
        itemVO.setTargetType("REPLY");
        itemVO.setPostId(reply == null ? null : reply.getPostId());
        itemVO.setPostTitle(post == null ? "原帖已不可见" : post.getTitle());
        itemVO.setTargetTitle(post == null ? "点赞的回复" : "回复于《" + post.getTitle() + "》");
        itemVO.setContentPreview(reply == null ? "该回复已不可见" : stripHtml(reply.getContent()));
        itemVO.setLikeCount(reply == null ? 0 : defaultZero(reply.getLikeCount()));
        itemVO.setStatus(reply == null ? 0 : reply.getStatus());
        itemVO.setVisibility(reply == null ? 0 : reply.getVisibility());
        return itemVO;
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
