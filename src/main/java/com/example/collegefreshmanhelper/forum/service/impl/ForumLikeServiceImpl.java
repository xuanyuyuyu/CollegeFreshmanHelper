package com.example.collegefreshmanhelper.forum.service.impl;

import com.example.collegefreshmanhelper.common.exception.BusinessException;
import com.example.collegefreshmanhelper.forum.entity.ForumPost;
import com.example.collegefreshmanhelper.forum.entity.ForumReply;
import com.example.collegefreshmanhelper.forum.mapper.ForumPostMapper;
import com.example.collegefreshmanhelper.forum.mapper.ForumReplyMapper;
import com.example.collegefreshmanhelper.forum.service.ForumLikeService;
import com.example.collegefreshmanhelper.forum.vo.LikeToggleVO;
import com.example.collegefreshmanhelper.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ForumLikeServiceImpl implements ForumLikeService {

    private static final String POST_LIKE_KEY_PREFIX = "forum:like:post:users:";
    private static final String REPLY_LIKE_KEY_PREFIX = "forum:like:reply:users:";
    private static final String POST_COUNT_KEY = "forum:like:post:count";
    private static final String REPLY_COUNT_KEY = "forum:like:reply:count";
    private static final String POST_DIRTY_KEY = "forum:like:post:dirty";
    private static final String REPLY_DIRTY_KEY = "forum:like:reply:dirty";

    private final StringRedisTemplate stringRedisTemplate;
    private final ForumPostMapper forumPostMapper;
    private final ForumReplyMapper forumReplyMapper;
    private final UserService userService;

    @Override
    public LikeToggleVO likePost(Long postId, Long currentUserId) {
        userService.getActiveUserById(currentUserId);
        ForumPost post = forumPostMapper.selectById(postId);
        validatePost(post);
        String key = postLikeKey(postId);
        Long added = stringRedisTemplate.opsForSet().add(key, String.valueOf(currentUserId));
        if (Long.valueOf(1L).equals(added)) {
            int currentCount = ensurePostCount(post);
            setPostCount(postId, currentCount + 1);
        }
        markPostDirty(postId);
        stringRedisTemplate.expire(key, 90, TimeUnit.DAYS);
        return new LikeToggleVO(true, getPostLikeCount(postId));
    }

    @Override
    public LikeToggleVO unlikePost(Long postId, Long currentUserId) {
        userService.getActiveUserById(currentUserId);
        ForumPost post = forumPostMapper.selectById(postId);
        validatePost(post);
        Long removed = stringRedisTemplate.opsForSet().remove(postLikeKey(postId), String.valueOf(currentUserId));
        if (removed != null && removed > 0) {
            int currentCount = ensurePostCount(post);
            setPostCount(postId, Math.max(0, currentCount - 1));
        }
        markPostDirty(postId);
        return new LikeToggleVO(false, getPostLikeCount(postId));
    }

    @Override
    public LikeToggleVO likeReply(Long replyId, Long currentUserId) {
        userService.getActiveUserById(currentUserId);
        ForumReply reply = forumReplyMapper.selectById(replyId);
        validateReply(reply);
        String key = replyLikeKey(replyId);
        Long added = stringRedisTemplate.opsForSet().add(key, String.valueOf(currentUserId));
        if (Long.valueOf(1L).equals(added)) {
            int currentCount = ensureReplyCount(reply);
            setReplyCount(replyId, currentCount + 1);
        }
        markReplyDirty(replyId);
        stringRedisTemplate.expire(key, 90, TimeUnit.DAYS);
        return new LikeToggleVO(true, getReplyLikeCount(replyId));
    }

    @Override
    public LikeToggleVO unlikeReply(Long replyId, Long currentUserId) {
        userService.getActiveUserById(currentUserId);
        ForumReply reply = forumReplyMapper.selectById(replyId);
        validateReply(reply);
        Long removed = stringRedisTemplate.opsForSet().remove(replyLikeKey(replyId), String.valueOf(currentUserId));
        if (removed != null && removed > 0) {
            int currentCount = ensureReplyCount(reply);
            setReplyCount(replyId, Math.max(0, currentCount - 1));
        }
        markReplyDirty(replyId);
        return new LikeToggleVO(false, getReplyLikeCount(replyId));
    }

    @Override
    public boolean hasPostLike(Long postId, Long userId) {
        if (userId == null) {
            return false;
        }
        Boolean member = stringRedisTemplate.opsForSet().isMember(postLikeKey(postId), String.valueOf(userId));
        return Boolean.TRUE.equals(member);
    }

    @Override
    public boolean hasReplyLike(Long replyId, Long userId) {
        if (userId == null) {
            return false;
        }
        Boolean member = stringRedisTemplate.opsForSet().isMember(replyLikeKey(replyId), String.valueOf(userId));
        return Boolean.TRUE.equals(member);
    }

    @Override
    public Map<Long, Boolean> batchPostLikeStatus(Collection<Long> postIds, Long userId) {
        if (userId == null || postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Boolean> result = new LinkedHashMap<>();
        for (Long postId : postIds) {
            result.put(postId, hasPostLike(postId, userId));
        }
        return result;
    }

    @Override
    public Map<Long, Boolean> batchReplyLikeStatus(Collection<Long> replyIds, Long userId) {
        if (userId == null || replyIds == null || replyIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Boolean> result = new HashMap<>();
        for (Long replyId : replyIds) {
            result.put(replyId, hasReplyLike(replyId, userId));
        }
        return result;
    }

    @Override
    public int getPostLikeCount(Long postId) {
        Object cached = stringRedisTemplate.opsForHash().get(POST_COUNT_KEY, String.valueOf(postId));
        if (cached != null) {
            return Integer.parseInt(String.valueOf(cached));
        }
        ForumPost post = forumPostMapper.selectById(postId);
        return post == null || post.getLikeCount() == null ? 0 : post.getLikeCount();
    }

    @Override
    public int getReplyLikeCount(Long replyId) {
        Object cached = stringRedisTemplate.opsForHash().get(REPLY_COUNT_KEY, String.valueOf(replyId));
        if (cached != null) {
            return Integer.parseInt(String.valueOf(cached));
        }
        ForumReply reply = forumReplyMapper.selectById(replyId);
        return reply == null || reply.getLikeCount() == null ? 0 : reply.getLikeCount();
    }

    @Override
    @Scheduled(fixedDelay = 15000L)
    public void flushDirtyLikeCounts() {
        flushPostLikes();
        flushReplyLikes();
    }

    private void flushPostLikes() {
        Set<String> dirtyPostIds = stringRedisTemplate.opsForSet().members(POST_DIRTY_KEY);
        if (dirtyPostIds == null || dirtyPostIds.isEmpty()) {
            return;
        }
        for (String postIdValue : dirtyPostIds) {
            Long postId = Long.valueOf(postIdValue);
            int count = getPostLikeCount(postId);
            ForumPost update = new ForumPost();
            update.setId(postId);
            update.setLikeCount(count);
            forumPostMapper.updateById(update);
            stringRedisTemplate.opsForSet().remove(POST_DIRTY_KEY, postIdValue);
        }
    }

    private void flushReplyLikes() {
        Set<String> dirtyReplyIds = stringRedisTemplate.opsForSet().members(REPLY_DIRTY_KEY);
        if (dirtyReplyIds == null || dirtyReplyIds.isEmpty()) {
            return;
        }
        for (String replyIdValue : dirtyReplyIds) {
            Long replyId = Long.valueOf(replyIdValue);
            int count = getReplyLikeCount(replyId);
            ForumReply update = new ForumReply();
            update.setId(replyId);
            update.setLikeCount(count);
            forumReplyMapper.updateById(update);
            stringRedisTemplate.opsForSet().remove(REPLY_DIRTY_KEY, replyIdValue);
        }
    }

    private void validatePost(ForumPost post) {
        if (post == null || Integer.valueOf(1).equals(post.getDeleted())) {
            throw new BusinessException("帖子不存在");
        }
        if (!Integer.valueOf(1).equals(post.getVisibility()) || !Integer.valueOf(1).equals(post.getStatus())) {
            throw new BusinessException("帖子当前不可点赞");
        }
    }

    private void validateReply(ForumReply reply) {
        if (reply == null || Integer.valueOf(1).equals(reply.getDeleted())) {
            throw new BusinessException("回复不存在");
        }
        if (!Integer.valueOf(1).equals(reply.getVisibility()) || !Integer.valueOf(1).equals(reply.getStatus())) {
            throw new BusinessException("回复当前不可点赞");
        }
    }

    private void markPostDirty(Long postId) {
        stringRedisTemplate.opsForSet().add(POST_DIRTY_KEY, String.valueOf(postId));
    }

    private void markReplyDirty(Long replyId) {
        stringRedisTemplate.opsForSet().add(REPLY_DIRTY_KEY, String.valueOf(replyId));
    }

    private String postLikeKey(Long postId) {
        return POST_LIKE_KEY_PREFIX + postId;
    }

    private String replyLikeKey(Long replyId) {
        return REPLY_LIKE_KEY_PREFIX + replyId;
    }

    private int ensurePostCount(ForumPost post) {
        Object cached = stringRedisTemplate.opsForHash().get(POST_COUNT_KEY, String.valueOf(post.getId()));
        if (cached != null) {
            return Integer.parseInt(String.valueOf(cached));
        }
        int count = post.getLikeCount() == null ? 0 : post.getLikeCount();
        setPostCount(post.getId(), count);
        return count;
    }

    private int ensureReplyCount(ForumReply reply) {
        Object cached = stringRedisTemplate.opsForHash().get(REPLY_COUNT_KEY, String.valueOf(reply.getId()));
        if (cached != null) {
            return Integer.parseInt(String.valueOf(cached));
        }
        int count = reply.getLikeCount() == null ? 0 : reply.getLikeCount();
        setReplyCount(reply.getId(), count);
        return count;
    }

    private void setPostCount(Long postId, int count) {
        stringRedisTemplate.opsForHash().put(POST_COUNT_KEY, String.valueOf(postId), String.valueOf(count));
    }

    private void setReplyCount(Long replyId, int count) {
        stringRedisTemplate.opsForHash().put(REPLY_COUNT_KEY, String.valueOf(replyId), String.valueOf(count));
    }
}
