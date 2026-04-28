package com.example.collegefreshmanhelper.admin.service;

import com.example.collegefreshmanhelper.admin.vo.AdminPostVO;
import com.example.collegefreshmanhelper.admin.vo.AdminReplyVO;
import com.example.collegefreshmanhelper.common.model.PageResult;
import com.example.collegefreshmanhelper.forum.entity.ForumPost;
import com.example.collegefreshmanhelper.forum.entity.ForumReply;

public interface AdminContentService {

    PageResult<AdminPostVO> pagePosts(long pageNum, long pageSize, String keyword, Integer status, Integer visibility, Long userId);

    PageResult<AdminReplyVO> pageReplies(long pageNum, long pageSize, String keyword, Long postId, Integer status, Integer visibility, Long userId);

    ForumPost updatePostVisibility(Long adminUserId, Long postId, boolean visible, String reason);

    ForumPost deletePost(Long adminUserId, Long postId, String reason);

    ForumReply updateReplyVisibility(Long adminUserId, Long replyId, boolean visible, String reason);

    ForumReply deleteReply(Long adminUserId, Long replyId, String reason);
}
