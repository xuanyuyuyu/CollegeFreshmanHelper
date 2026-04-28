package com.example.collegefreshmanhelper.forum.service;

import com.example.collegefreshmanhelper.forum.vo.LikeToggleVO;

import java.util.Collection;
import java.util.Map;

public interface ForumLikeService {

    LikeToggleVO likePost(Long postId, Long currentUserId);

    LikeToggleVO unlikePost(Long postId, Long currentUserId);

    LikeToggleVO likeReply(Long replyId, Long currentUserId);

    LikeToggleVO unlikeReply(Long replyId, Long currentUserId);

    boolean hasPostLike(Long postId, Long userId);

    boolean hasReplyLike(Long replyId, Long userId);

    Map<Long, Boolean> batchPostLikeStatus(Collection<Long> postIds, Long userId);

    Map<Long, Boolean> batchReplyLikeStatus(Collection<Long> replyIds, Long userId);

    int getPostLikeCount(Long postId);

    int getReplyLikeCount(Long replyId);

    void flushDirtyLikeCounts();
}
