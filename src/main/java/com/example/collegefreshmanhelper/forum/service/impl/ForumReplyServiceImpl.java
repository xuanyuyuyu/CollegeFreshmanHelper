package com.example.collegefreshmanhelper.forum.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.collegefreshmanhelper.common.exception.BusinessException;
import com.example.collegefreshmanhelper.forum.dto.ForumReplyCreateRequest;
import com.example.collegefreshmanhelper.forum.entity.ForumPost;
import com.example.collegefreshmanhelper.forum.entity.ForumReply;
import com.example.collegefreshmanhelper.forum.mapper.ForumReplyMapper;
import com.example.collegefreshmanhelper.forum.service.ForumLikeService;
import com.example.collegefreshmanhelper.forum.service.ForumPostService;
import com.example.collegefreshmanhelper.forum.service.ForumReplyService;
import com.example.collegefreshmanhelper.forum.vo.ForumReplyThreadVO;
import com.example.collegefreshmanhelper.forum.vo.ForumReplyVO;
import com.example.collegefreshmanhelper.user.entity.SysUser;
import com.example.collegefreshmanhelper.user.entity.UserStats;
import com.example.collegefreshmanhelper.user.service.UserService;
import com.example.collegefreshmanhelper.user.service.UserStatsService;
import com.example.collegefreshmanhelper.user.service.UserTitleDisplayService;
import com.example.collegefreshmanhelper.user.service.UserTitleMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumReplyServiceImpl extends ServiceImpl<ForumReplyMapper, ForumReply> implements ForumReplyService {

    private static final String USER_DELETE_REASON = "用户自行删除";

    private final ForumPostService forumPostService;
    private final ForumLikeService forumLikeService;
    private final UserService userService;
    private final UserStatsService userStatsService;
    private final UserTitleDisplayService userTitleDisplayService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumReply createReply(Long postId, Long currentUserId, ForumReplyCreateRequest request) {
        userService.getActiveUserById(currentUserId);
        ForumPost post = forumPostService.getById(postId);
        if (post == null || Integer.valueOf(1).equals(post.getDeleted())) {
            throw new BusinessException("帖子不存在");
        }

        ResolvedReplyTarget resolvedTarget = resolveReplyTarget(postId, request);

        ForumReply reply = new ForumReply();
        reply.setPostId(postId);
        reply.setUserId(currentUserId);
        reply.setParentId(resolvedTarget.parentId());
        reply.setReplyToReplyId(resolvedTarget.replyToReplyId());
        reply.setReplyToUserId(resolvedTarget.replyToUserId());
        reply.setContent(request.getContent());
        reply.setContentType(request.getImageUrl() == null || request.getImageUrl().isBlank() ? 1 : 2);
        reply.setImageUrl(request.getImageUrl());
        reply.setLikeCount(0);
        reply.setChildCount(0);
        reply.setStatus(1);
        reply.setVisibility(1);
        reply.setQaSyncStatus(0);
        reply.setDeleted(0);

        if (resolvedTarget.parentId() > 0) {
            lambdaUpdate()
                    .eq(ForumReply::getId, resolvedTarget.parentId())
                    .setSql("child_count = child_count + 1")
                    .update();
        }

        save(reply);

        forumPostService.lambdaUpdate()
                .eq(ForumPost::getId, postId)
                .setSql("reply_count = reply_count + 1")
                .set(ForumPost::getLastReplyAt, LocalDateTime.now())
                .update();
        userTitleDisplayService.syncUserTitle(currentUserId);

        return reply;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOwnReply(Long postId, Long currentUserId, Long replyId) {
        userService.getActiveUserById(currentUserId);
        ForumReply reply = getById(replyId);
        if (reply == null || Integer.valueOf(1).equals(reply.getDeleted()) || !Objects.equals(reply.getPostId(), postId)) {
            throw new BusinessException("回复不存在");
        }
        if (!Objects.equals(reply.getUserId(), currentUserId)) {
            throw new BusinessException("只能删除自己发布的回复");
        }

        boolean isRootReply = reply.getParentId() == null || reply.getParentId() == 0;
        if (isRootReply) {
            List<Long> deleteIds = lambdaQuery()
                    .eq(ForumReply::getPostId, postId)
                    .eq(ForumReply::getDeleted, 0)
                    .and(wrapper -> wrapper.eq(ForumReply::getId, replyId).or().eq(ForumReply::getParentId, replyId))
                    .list()
                    .stream()
                    .map(ForumReply::getId)
                    .toList();
            if (deleteIds.isEmpty()) {
                throw new BusinessException("回复不存在");
            }
            lambdaUpdate()
                    .in(ForumReply::getId, deleteIds)
                    .set(ForumReply::getDeleted, 1)
                    .set(ForumReply::getVisibility, 0)
                    .set(ForumReply::getManualDeleteReason, USER_DELETE_REASON)
                    .update();
            decrementPostReplyCount(postId, deleteIds.size());
            userTitleDisplayService.syncUserTitle(currentUserId);
            return;
        }

        lambdaUpdate()
                .eq(ForumReply::getId, replyId)
                .eq(ForumReply::getDeleted, 0)
                .set(ForumReply::getDeleted, 1)
                .set(ForumReply::getVisibility, 0)
                .set(ForumReply::getManualDeleteReason, USER_DELETE_REASON)
                .update();

        decrementPostReplyCount(postId, 1);
        lambdaUpdate()
                .eq(ForumReply::getId, reply.getParentId())
                .eq(ForumReply::getDeleted, 0)
                .setSql("child_count = GREATEST(IFNULL(child_count, 0) - 1, 0)")
                .update();
        userTitleDisplayService.syncUserTitle(currentUserId);
    }

    private ResolvedReplyTarget resolveReplyTarget(Long postId, ForumReplyCreateRequest request) {
        Long replyToReplyId = request.getReplyToReplyId();
        Long parentId = request.getParentId();

        if (replyToReplyId == null && parentId == null) {
            return new ResolvedReplyTarget(0L, null, null);
        }

        Long targetReplyId = replyToReplyId != null ? replyToReplyId : parentId;
        if (targetReplyId == null || targetReplyId <= 0) {
            throw new BusinessException("目标回复参数非法");
        }

        ForumReply targetReply = getById(targetReplyId);
        if (targetReply == null || Integer.valueOf(1).equals(targetReply.getDeleted()) || !postId.equals(targetReply.getPostId())) {
            throw new BusinessException("目标回复不存在");
        }

        Long rootReplyId = targetReply.getParentId() == null || targetReply.getParentId() == 0
                ? targetReply.getId()
                : targetReply.getParentId();

        return new ResolvedReplyTarget(rootReplyId, targetReply.getId(), targetReply.getUserId());
    }

    private record ResolvedReplyTarget(Long parentId, Long replyToReplyId, Long replyToUserId) {
    }

    @Override
    public List<ForumReplyThreadVO> listPublishedRepliesByPostId(Long postId, Long currentUserId) {
        ForumPost post = forumPostService.getById(postId);
        if (post == null || Integer.valueOf(1).equals(post.getDeleted())) {
            throw new BusinessException("帖子不存在");
        }
        List<ForumReply> replies = lambdaQuery()
                .eq(ForumReply::getPostId, postId)
                .eq(ForumReply::getDeleted, 0)
                .eq(ForumReply::getVisibility, 1)
                .eq(ForumReply::getStatus, 1)
                .orderByAsc(ForumReply::getParentId)
                .orderByAsc(ForumReply::getCreatedAt)
                .list();
        if (replies.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, SysUser> userMap = loadReplyUsers(replies);
        userTitleDisplayService.syncUserTitles(userMap.keySet());
        Map<Long, UserStats> statsMap = loadReplyStats(userMap.keySet());
        Map<Long, UserTitleMetrics> metricsMap = userTitleDisplayService.buildMetricsMap(userMap.keySet(), statsMap);
        Map<Long, String> titleMap = userTitleDisplayService.findWearingTitles(userMap.keySet());
        Map<Long, Boolean> likeStatusMap = forumLikeService.batchReplyLikeStatus(replies.stream().map(ForumReply::getId).collect(Collectors.toSet()), currentUserId);
        Map<Long, ForumReplyVO> rootReplyMap = new LinkedHashMap<>();
        Map<Long, List<ForumReplyVO>> childReplyMap = new LinkedHashMap<>();

        for (ForumReply reply : replies) {
            ForumReplyVO replyVO = toReplyVO(reply, post.getUserId(), userMap, metricsMap, titleMap, likeStatusMap);
            if (reply.getParentId() == null || reply.getParentId() == 0) {
                rootReplyMap.put(reply.getId(), replyVO);
                childReplyMap.putIfAbsent(reply.getId(), new ArrayList<>());
            } else {
                childReplyMap.computeIfAbsent(reply.getParentId(), key -> new ArrayList<>()).add(replyVO);
            }
        }

        List<ForumReplyThreadVO> threads = new ArrayList<>();
        for (Map.Entry<Long, ForumReplyVO> entry : rootReplyMap.entrySet()) {
            threads.add(new ForumReplyThreadVO(
                    entry.getValue(),
                    childReplyMap.getOrDefault(entry.getKey(), Collections.emptyList())
            ));
        }
        return threads;
    }

    private Map<Long, SysUser> loadReplyUsers(Collection<ForumReply> replies) {
        Set<Long> userIds = replies.stream()
                .flatMap(reply -> java.util.stream.Stream.of(reply.getUserId(), reply.getReplyToUserId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user));
    }

    private Map<Long, UserStats> loadReplyStats(Collection<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userStatsService.listByIds(userIds).stream()
                .collect(Collectors.toMap(UserStats::getUserId, stats -> stats));
    }

    private ForumReplyVO toReplyVO(ForumReply reply, Long postAuthorId, Map<Long, SysUser> userMap, Map<Long, UserTitleMetrics> metricsMap, Map<Long, String> titleMap, Map<Long, Boolean> likeStatusMap) {
        SysUser author = userMap.get(reply.getUserId());
        SysUser replyToUser = userMap.get(reply.getReplyToUserId());

        ForumReplyVO replyVO = new ForumReplyVO();
        replyVO.setId(reply.getId());
        replyVO.setPostId(reply.getPostId());
        replyVO.setUserId(reply.getUserId());
        replyVO.setUserNickname(author == null ? null : author.getNickname());
        replyVO.setUserAvatarUrl(author == null ? null : author.getAvatarUrl());
        replyVO.setUserTitle(resolveDisplayedTitle(reply.getUserId(), titleMap, metricsMap));
        replyVO.setUserCustomTitle(hasCustomTitle(reply.getUserId(), titleMap));
        replyVO.setPostAuthor(Objects.equals(reply.getUserId(), postAuthorId));
        replyVO.setParentId(reply.getParentId());
        replyVO.setReplyToReplyId(reply.getReplyToReplyId());
        replyVO.setReplyToUserId(reply.getReplyToUserId());
        replyVO.setReplyToUserNickname(replyToUser == null ? null : replyToUser.getNickname());
        replyVO.setReplyToUserTitle(resolveDisplayedTitle(reply.getReplyToUserId(), titleMap, metricsMap));
        replyVO.setReplyToUserCustomTitle(hasCustomTitle(reply.getReplyToUserId(), titleMap));
        replyVO.setContent(reply.getContent());
        replyVO.setContentType(reply.getContentType());
        replyVO.setImageUrl(reply.getImageUrl());
        replyVO.setLikeCount(reply.getLikeCount());
        replyVO.setLiked(Boolean.TRUE.equals(likeStatusMap.get(reply.getId())));
        replyVO.setChildCount(reply.getChildCount());
        replyVO.setCreatedAt(reply.getCreatedAt());
        return replyVO;
    }

    private String resolveDisplayedTitle(Long userId, Map<Long, String> titleMap, Map<Long, UserTitleMetrics> metricsMap) {
        if (userId != null) {
            String wearingTitle = titleMap.get(userId);
            if (wearingTitle != null) {
                return wearingTitle;
            }
        }
        return userTitleDisplayService.resolveAutomaticTitle(metricsMap.get(userId));
    }

    private boolean hasCustomTitle(Long userId, Map<Long, String> titleMap) {
        return userId != null && titleMap.get(userId) != null;
    }

    private void decrementPostReplyCount(Long postId, int count) {
        forumPostService.lambdaUpdate()
                .eq(ForumPost::getId, postId)
                .setSql("reply_count = GREATEST(IFNULL(reply_count, 0) - " + count + ", 0)")
                .update();
    }
}
