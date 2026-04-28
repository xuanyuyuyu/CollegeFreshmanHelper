package com.example.collegefreshmanhelper.forum.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.collegefreshmanhelper.common.exception.BusinessException;
import com.example.collegefreshmanhelper.forum.dto.ForumPostCreateRequest;
import com.example.collegefreshmanhelper.forum.entity.ForumPost;
import com.example.collegefreshmanhelper.forum.entity.ForumPostImage;
import com.example.collegefreshmanhelper.forum.mapper.ForumPostMapper;
import com.example.collegefreshmanhelper.forum.service.ForumLikeService;
import com.example.collegefreshmanhelper.forum.service.ForumPostService;
import com.example.collegefreshmanhelper.forum.vo.ForumAuthorVO;
import com.example.collegefreshmanhelper.forum.vo.ForumPostDetailVO;
import com.example.collegefreshmanhelper.forum.vo.ForumPostSummaryVO;
import com.example.collegefreshmanhelper.user.entity.SysUser;
import com.example.collegefreshmanhelper.user.entity.UserStats;
import com.example.collegefreshmanhelper.user.service.UserService;
import com.example.collegefreshmanhelper.user.service.UserStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumPostServiceImpl extends ServiceImpl<ForumPostMapper, ForumPost> implements ForumPostService {

    private final ForumPostImageServiceImpl forumPostImageService;
    private final ForumLikeService forumLikeService;
    private final UserService userService;
    private final UserStatsService userStatsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumPost createPost(Long currentUserId, ForumPostCreateRequest request) {
        userService.getActiveUserById(currentUserId);
        List<String> imageUrls = request.getImageUrls() == null ? Collections.emptyList() : request.getImageUrls();
        boolean hasImages = !imageUrls.isEmpty();
        LocalDateTime now = LocalDateTime.now();

        ForumPost post = new ForumPost();
        post.setUserId(currentUserId);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setContentType(hasImages ? 2 : 1);
        post.setFirstImageUrl(hasImages ? imageUrls.get(0) : null);
        post.setImageCount(imageUrls.size());
        post.setTags(request.getTags());
        post.setViewCount(0);
        post.setReplyCount(0);
        post.setLikeCount(0);
        post.setCollectCount(0);
        post.setStatus(hasImages ? 0 : 1);
        post.setVisibility(1);
        post.setPublishedAt(hasImages ? null : now);
        post.setLastReplyAt(null);
        post.setDeleted(0);
        save(post);

        if (hasImages) {
            for (int i = 0; i < imageUrls.size(); i++) {
                ForumPostImage image = new ForumPostImage();
                image.setPostId(post.getId());
                image.setImageUrl(imageUrls.get(i));
                image.setSortNo(i + 1);
                image.setStatus(0);
                image.setDeleted(0);
                forumPostImageService.save(image);
            }
        }
        return post;
    }

    @Override
    public ForumPostDetailVO getPostDetail(Long postId, boolean incrementView, Long currentUserId) {
        ForumPost post = lambdaQuery()
                .eq(ForumPost::getId, postId)
                .eq(ForumPost::getDeleted, 0)
                .one();
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        if (incrementView) {
            lambdaUpdate()
                    .eq(ForumPost::getId, postId)
                    .setSql("view_count = IFNULL(view_count, 0) + 1")
                    .update();
            post.setViewCount(defaultZero(post.getViewCount()) + 1);
        }

        List<String> imageUrls = forumPostImageService.lambdaQuery()
                .eq(ForumPostImage::getPostId, postId)
                .eq(ForumPostImage::getDeleted, 0)
                .orderByAsc(ForumPostImage::getSortNo)
                .list()
                .stream()
                .map(ForumPostImage::getImageUrl)
                .toList();
        ForumAuthorVO author = buildAuthor(post.getUserId());
        return new ForumPostDetailVO(post, author, forumLikeService.hasPostLike(postId, currentUserId), imageUrls);
    }

    @Override
    public Page<ForumPostSummaryVO> pagePublishedPosts(long pageNum, long pageSize, String sortType, Long currentUserId) {
        String normalizedSortType = sortType == null ? "latest" : sortType.trim().toLowerCase(Locale.ROOT);
        Page<ForumPost> page = new Page<>(pageNum, pageSize);
        var query = lambdaQuery()
                .eq(ForumPost::getDeleted, 0)
                .eq(ForumPost::getVisibility, 1)
                .eq(ForumPost::getStatus, 1);
        if ("hottest".equals(normalizedSortType)) {
            query.orderByDesc(ForumPost::getLikeCount)
                    .orderByDesc(ForumPost::getReplyCount)
                    .orderByDesc(ForumPost::getViewCount)
                    .orderByDesc(ForumPost::getPublishedAt)
                    .orderByDesc(ForumPost::getCreatedAt);
        } else {
            query.orderByDesc(ForumPost::getPublishedAt)
                    .orderByDesc(ForumPost::getCreatedAt);
        }
        query.page(page);
        Map<Long, SysUser> userMap = loadUserMap(page.getRecords().stream().map(ForumPost::getUserId).collect(Collectors.toSet()));
        Map<Long, UserStats> statsMap = loadStatsMap(page.getRecords().stream().map(ForumPost::getUserId).collect(Collectors.toSet()));
        Map<Long, Boolean> likeStatusMap = forumLikeService.batchPostLikeStatus(page.getRecords().stream().map(ForumPost::getId).collect(Collectors.toSet()), currentUserId);

        Page<ForumPostSummaryVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(page.getRecords().stream().map(post -> toSummaryVO(post, userMap, statsMap, likeStatusMap)).toList());
        return resultPage;
    }

    private ForumPostSummaryVO toSummaryVO(ForumPost post, Map<Long, SysUser> userMap, Map<Long, UserStats> statsMap, Map<Long, Boolean> likeStatusMap) {
        ForumPostSummaryVO summaryVO = new ForumPostSummaryVO();
        summaryVO.setId(post.getId());
        summaryVO.setTitle(post.getTitle());
        summaryVO.setContentPreview(stripHtml(post.getContent()));
        summaryVO.setTags(post.getTags());
        summaryVO.setReplyCount(defaultZero(post.getReplyCount()));
        summaryVO.setLikeCount(defaultZero(post.getLikeCount()));
        summaryVO.setViewCount(defaultZero(post.getViewCount()));
        summaryVO.setImageCount(defaultZero(post.getImageCount()));
        summaryVO.setFirstImageUrl(post.getFirstImageUrl());
        summaryVO.setLiked(Boolean.TRUE.equals(likeStatusMap.get(post.getId())));
        summaryVO.setPublishedAt(post.getPublishedAt());
        summaryVO.setCreatedAt(post.getCreatedAt());
        summaryVO.setAuthor(buildAuthor(post.getUserId(), userMap, statsMap));
        return summaryVO;
    }

    private ForumAuthorVO buildAuthor(Long userId) {
        Map<Long, SysUser> userMap = loadUserMap(Collections.singleton(userId));
        Map<Long, UserStats> statsMap = loadStatsMap(Collections.singleton(userId));
        return buildAuthor(userId, userMap, statsMap);
    }

    private ForumAuthorVO buildAuthor(Long userId, Map<Long, SysUser> userMap, Map<Long, UserStats> statsMap) {
        SysUser user = userMap.get(userId);
        UserStats stats = statsMap.get(userId);
        if (user == null) {
            return new ForumAuthorVO(userId, "匿名用户", null, "新生伙伴");
        }
        return new ForumAuthorVO(
                user.getId(),
                user.getNickname(),
                user.getAvatarUrl(),
                resolveTitle(stats)
        );
    }

    private Map<Long, SysUser> loadUserMap(java.util.Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
    }

    private Map<Long, UserStats> loadStatsMap(java.util.Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userStatsService.listByIds(userIds).stream()
                .collect(Collectors.toMap(UserStats::getUserId, Function.identity()));
    }

    private String resolveTitle(UserStats stats) {
        if (stats == null) {
            return "新生伙伴";
        }
        int postLikes = defaultZero(stats.getPostLikeReceivedCount());
        int replyLikes = defaultZero(stats.getReplyLikeReceivedCount());
        int totalLikes = postLikes + replyLikes;
        int featuredAnswers = defaultZero(stats.getFeaturedAnswerCount());
        int contributionCount = defaultZero(stats.getKnowledgeContributionCount());
        int replyCount = defaultZero(stats.getReplyCount());
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

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private String stripHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }
}
