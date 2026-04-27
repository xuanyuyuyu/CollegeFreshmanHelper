package com.example.collegefreshmanhelper.admin.service;

import com.example.collegefreshmanhelper.forum.entity.ForumPost;
import com.example.collegefreshmanhelper.forum.entity.ForumReply;

public interface AdminContentService {

    ForumPost updatePostVisibility(Long adminUserId, Long postId, boolean visible, String reason);

    ForumPost deletePost(Long adminUserId, Long postId, String reason);

    ForumReply updateReplyVisibility(Long adminUserId, Long replyId, boolean visible, String reason);

    ForumReply deleteReply(Long adminUserId, Long replyId, String reason);
}
