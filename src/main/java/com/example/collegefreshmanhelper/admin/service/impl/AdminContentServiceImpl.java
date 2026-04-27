package com.example.collegefreshmanhelper.admin.service.impl;

import com.example.collegefreshmanhelper.admin.service.AdminContentService;
import com.example.collegefreshmanhelper.admin.service.AdminOperationLogService;
import com.example.collegefreshmanhelper.common.exception.BusinessException;
import com.example.collegefreshmanhelper.forum.entity.ForumPost;
import com.example.collegefreshmanhelper.forum.entity.ForumReply;
import com.example.collegefreshmanhelper.forum.service.ForumPostService;
import com.example.collegefreshmanhelper.forum.service.ForumReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminContentServiceImpl implements AdminContentService {

    private final ForumPostService forumPostService;
    private final ForumReplyService forumReplyService;
    private final AdminOperationLogService adminOperationLogService;

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
