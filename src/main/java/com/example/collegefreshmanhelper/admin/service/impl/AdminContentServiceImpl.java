package com.example.collegefreshmanhelper.admin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.collegefreshmanhelper.admin.service.AdminContentService;
import com.example.collegefreshmanhelper.admin.service.AdminOperationLogService;
import com.example.collegefreshmanhelper.admin.vo.AdminPostVO;
import com.example.collegefreshmanhelper.admin.vo.AdminReplyVO;
import com.example.collegefreshmanhelper.common.model.PageResult;
import com.example.collegefreshmanhelper.common.exception.BusinessException;
import com.example.collegefreshmanhelper.forum.entity.ForumPost;
import com.example.collegefreshmanhelper.forum.entity.ForumReply;
import com.example.collegefreshmanhelper.forum.service.ForumPostService;
import com.example.collegefreshmanhelper.forum.service.ForumReplyService;
import com.example.collegefreshmanhelper.user.entity.SysUser;
import com.example.collegefreshmanhelper.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminContentServiceImpl implements AdminContentService {

    private final ForumPostService forumPostService;
    private final ForumReplyService forumReplyService;
    private final UserService userService;
    private final AdminOperationLogService adminOperationLogService;

    @Override
    public PageResult<AdminPostVO> pagePosts(long pageNum, long pageSize, String keyword, Integer status, Integer visibility, Long userId) {
        Page<ForumPost> page = new Page<>(pageNum, pageSize);
        var query = forumPostService.lambdaQuery();
        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();
            Set<Long> matchedUserIds = searchUserIds(trimmedKeyword);
            query.and(wrapper -> {
                wrapper.like(ForumPost::getTitle, trimmedKeyword)
                        .or()
                        .like(ForumPost::getContent, trimmedKeyword);
                if (!matchedUserIds.isEmpty()) {
                    wrapper.or().in(ForumPost::getUserId, matchedUserIds);
                }
            });
        }
        if (status != null) {
            query.eq(ForumPost::getStatus, status);
        }
        if (visibility != null) {
            query.eq(ForumPost::getVisibility, visibility);
        }
        if (userId != null) {
            query.eq(ForumPost::getUserId, userId);
        }
        query.orderByDesc(ForumPost::getCreatedAt).page(page);

        Map<Long, SysUser> userMap = loadUserMap(page.getRecords().stream().map(ForumPost::getUserId).collect(Collectors.toSet()));
        Page<AdminPostVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(page.getRecords().stream().map(post -> toPostVO(post, userMap.get(post.getUserId()))).toList());
        return PageResult.of(resultPage);
    }

    @Override
    public PageResult<AdminReplyVO> pageReplies(long pageNum, long pageSize, String keyword, Long postId, Integer status, Integer visibility, Long userId) {
        Page<ForumReply> page = new Page<>(pageNum, pageSize);
        var query = forumReplyService.lambdaQuery();
        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();
            Set<Long> matchedUserIds = searchUserIds(trimmedKeyword);
            Set<Long> matchedPostIds = searchPostIds(trimmedKeyword);
            query.and(wrapper -> {
                wrapper.like(ForumReply::getContent, trimmedKeyword);
                if (!matchedUserIds.isEmpty()) {
                    wrapper.or().in(ForumReply::getUserId, matchedUserIds)
                            .or().in(ForumReply::getReplyToUserId, matchedUserIds);
                }
                if (!matchedPostIds.isEmpty()) {
                    wrapper.or().in(ForumReply::getPostId, matchedPostIds);
                }
            });
        }
        if (postId != null) {
            query.eq(ForumReply::getPostId, postId);
        }
        if (status != null) {
            query.eq(ForumReply::getStatus, status);
        }
        if (visibility != null) {
            query.eq(ForumReply::getVisibility, visibility);
        }
        if (userId != null) {
            query.eq(ForumReply::getUserId, userId);
        }
        query.orderByDesc(ForumReply::getCreatedAt).page(page);

        Set<Long> postIds = page.getRecords().stream().map(ForumReply::getPostId).collect(Collectors.toSet());
        Set<Long> userIds = page.getRecords().stream()
                .flatMap(reply -> java.util.stream.Stream.of(reply.getUserId(), reply.getReplyToUserId()))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, ForumPost> postMap = loadPostMap(postIds);
        Map<Long, SysUser> userMap = loadUserMap(userIds);

        Page<AdminReplyVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(page.getRecords().stream().map(reply -> toReplyVO(reply, postMap, userMap)).toList());
        return PageResult.of(resultPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumPost updatePostVisibility(Long adminUserId, Long postId, boolean visible, String reason) {
        ForumPost post = forumPostService.getById(postId);
        if (post == null || Integer.valueOf(1).equals(post.getDeleted())) {
            throw new BusinessException("帖子不存在");
        }
        ForumPost before = copyPost(post);
        post.setVisibility(visible ? 1 : 0);
        post.setOperatorAdminId(adminUserId);
        post.setManualDeleteReason(reason);
        if (!visible) {
            post.setStatus(3);
        } else if (Integer.valueOf(3).equals(post.getStatus())) {
            post.setStatus(1);
        }
        forumPostService.updateById(post);
        adminOperationLogService.record(adminUserId, 2, postId, visible ? "SHOW_POST" : "HIDE_POST", before, post, reason);
        return post;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumPost deletePost(Long adminUserId, Long postId, String reason) {
        ForumPost post = forumPostService.getById(postId);
        if (post == null || Integer.valueOf(1).equals(post.getDeleted())) {
            throw new BusinessException("帖子不存在");
        }
        ForumPost before = copyPost(post);
        post.setDeleted(1);
        post.setVisibility(0);
        post.setOperatorAdminId(adminUserId);
        post.setManualDeleteReason(reason);
        forumPostService.updateById(post);
        adminOperationLogService.record(adminUserId, 2, postId, "DELETE_POST", before, post, reason);
        return post;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumReply updateReplyVisibility(Long adminUserId, Long replyId, boolean visible, String reason) {
        ForumReply reply = forumReplyService.getById(replyId);
        if (reply == null || Integer.valueOf(1).equals(reply.getDeleted())) {
            throw new BusinessException("回复不存在");
        }
        ForumReply before = copyReply(reply);
        reply.setVisibility(visible ? 1 : 0);
        reply.setOperatorAdminId(adminUserId);
        reply.setManualDeleteReason(reason);
        if (!visible) {
            reply.setStatus(3);
        } else if (Integer.valueOf(3).equals(reply.getStatus())) {
            reply.setStatus(1);
        }
        forumReplyService.updateById(reply);
        adminOperationLogService.record(adminUserId, 3, replyId, visible ? "SHOW_REPLY" : "HIDE_REPLY", before, reply, reason);
        return reply;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumReply deleteReply(Long adminUserId, Long replyId, String reason) {
        ForumReply reply = forumReplyService.getById(replyId);
        if (reply == null || Integer.valueOf(1).equals(reply.getDeleted())) {
            throw new BusinessException("回复不存在");
        }
        ForumReply before = copyReply(reply);
        reply.setDeleted(1);
        reply.setVisibility(0);
        reply.setOperatorAdminId(adminUserId);
        reply.setManualDeleteReason(reason);
        forumReplyService.updateById(reply);
        adminOperationLogService.record(adminUserId, 3, replyId, "DELETE_REPLY", before, reply, reason);
        return reply;
    }

    private Map<Long, SysUser> loadUserMap(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
    }

    private Map<Long, ForumPost> loadPostMap(Set<Long> postIds) {
        if (postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return forumPostService.listByIds(postIds).stream()
                .collect(Collectors.toMap(ForumPost::getId, Function.identity()));
    }

    private Set<Long> searchUserIds(String keyword) {
        return userService.lambdaQuery()
                .eq(SysUser::getDeleted, 0)
                .and(wrapper -> {
                    wrapper.like(SysUser::getUsername, keyword)
                            .or()
                            .like(SysUser::getNickname, keyword);
                    if (keyword.chars().allMatch(Character::isDigit)) {
                        wrapper.or().eq(SysUser::getId, Long.valueOf(keyword));
                    }
                })
                .list()
                .stream()
                .map(SysUser::getId)
                .collect(Collectors.toSet());
    }

    private Set<Long> searchPostIds(String keyword) {
        return forumPostService.lambdaQuery()
                .eq(ForumPost::getDeleted, 0)
                .and(wrapper -> {
                    wrapper.like(ForumPost::getTitle, keyword)
                            .or()
                            .like(ForumPost::getContent, keyword);
                    if (keyword.chars().allMatch(Character::isDigit)) {
                        wrapper.or().eq(ForumPost::getId, Long.valueOf(keyword));
                    }
                })
                .list()
                .stream()
                .map(ForumPost::getId)
                .collect(Collectors.toSet());
    }

    private AdminPostVO toPostVO(ForumPost post, SysUser author) {
        AdminPostVO vo = new AdminPostVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setAuthorNickname(author == null ? "未知用户" : author.getNickname());
        vo.setTitle(post.getTitle());
        vo.setContentPreview(stripHtml(post.getContent()));
        vo.setTags(post.getTags());
        vo.setStatus(post.getStatus());
        vo.setVisibility(post.getVisibility());
        vo.setDeleted(post.getDeleted());
        vo.setViewCount(defaultZero(post.getViewCount()));
        vo.setReplyCount(defaultZero(post.getReplyCount()));
        vo.setLikeCount(defaultZero(post.getLikeCount()));
        vo.setPublishedAt(post.getPublishedAt());
        vo.setLastReplyAt(post.getLastReplyAt());
        vo.setOperatorAdminId(post.getOperatorAdminId());
        vo.setManualDeleteReason(post.getManualDeleteReason());
        vo.setCreatedAt(post.getCreatedAt());
        return vo;
    }

    private AdminReplyVO toReplyVO(ForumReply reply, Map<Long, ForumPost> postMap, Map<Long, SysUser> userMap) {
        ForumPost post = postMap.get(reply.getPostId());
        SysUser author = userMap.get(reply.getUserId());
        SysUser replyToUser = userMap.get(reply.getReplyToUserId());
        AdminReplyVO vo = new AdminReplyVO();
        vo.setId(reply.getId());
        vo.setPostId(reply.getPostId());
        vo.setPostTitle(post == null ? null : post.getTitle());
        vo.setUserId(reply.getUserId());
        vo.setAuthorNickname(author == null ? "未知用户" : author.getNickname());
        vo.setParentId(reply.getParentId());
        vo.setReplyToReplyId(reply.getReplyToReplyId());
        vo.setReplyToUserId(reply.getReplyToUserId());
        vo.setReplyToUserNickname(replyToUser == null ? null : replyToUser.getNickname());
        vo.setContentPreview(stripHtml(reply.getContent()));
        vo.setStatus(reply.getStatus());
        vo.setVisibility(reply.getVisibility());
        vo.setDeleted(reply.getDeleted());
        vo.setLikeCount(defaultZero(reply.getLikeCount()));
        vo.setChildCount(defaultZero(reply.getChildCount()));
        vo.setQaSyncStatus(reply.getQaSyncStatus());
        vo.setOperatorAdminId(reply.getOperatorAdminId());
        vo.setManualDeleteReason(reply.getManualDeleteReason());
        vo.setCreatedAt(reply.getCreatedAt());
        return vo;
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

    private ForumPost copyPost(ForumPost source) {
        ForumPost target = new ForumPost();
        target.setId(source.getId());
        target.setStatus(source.getStatus());
        target.setVisibility(source.getVisibility());
        target.setDeleted(source.getDeleted());
        target.setOperatorAdminId(source.getOperatorAdminId());
        target.setManualDeleteReason(source.getManualDeleteReason());
        return target;
    }

    private ForumReply copyReply(ForumReply source) {
        ForumReply target = new ForumReply();
        target.setId(source.getId());
        target.setStatus(source.getStatus());
        target.setVisibility(source.getVisibility());
        target.setDeleted(source.getDeleted());
        target.setOperatorAdminId(source.getOperatorAdminId());
        target.setManualDeleteReason(source.getManualDeleteReason());
        return target;
    }
}
