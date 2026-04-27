package com.example.collegefreshmanhelper.forum.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.collegefreshmanhelper.common.exception.BusinessException;
import com.example.collegefreshmanhelper.forum.dto.ForumPostCreateRequest;
import com.example.collegefreshmanhelper.forum.entity.ForumPost;
import com.example.collegefreshmanhelper.forum.entity.ForumPostImage;
import com.example.collegefreshmanhelper.forum.mapper.ForumPostMapper;
import com.example.collegefreshmanhelper.forum.service.ForumPostService;
import com.example.collegefreshmanhelper.forum.vo.ForumPostDetailVO;
import com.example.collegefreshmanhelper.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ForumPostServiceImpl extends ServiceImpl<ForumPostMapper, ForumPost> implements ForumPostService {

    private final ForumPostImageServiceImpl forumPostImageService;
    private final UserService userService;

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
    public ForumPostDetailVO getPostDetail(Long postId) {
        ForumPost post = lambdaQuery()
                .eq(ForumPost::getId, postId)
                .eq(ForumPost::getDeleted, 0)
                .one();
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        List<String> imageUrls = forumPostImageService.lambdaQuery()
                .eq(ForumPostImage::getPostId, postId)
                .eq(ForumPostImage::getDeleted, 0)
                .orderByAsc(ForumPostImage::getSortNo)
                .list()
                .stream()
                .map(ForumPostImage::getImageUrl)
                .toList();
        return new ForumPostDetailVO(post, imageUrls);
    }

    @Override
    public Page<ForumPost> pagePublishedPosts(long pageNum, long pageSize) {
        Page<ForumPost> page = new Page<>(pageNum, pageSize);
        return lambdaQuery()
                .eq(ForumPost::getDeleted, 0)
                .eq(ForumPost::getVisibility, 1)
                .eq(ForumPost::getStatus, 1)
                .orderByDesc(ForumPost::getPublishedAt)
                .orderByDesc(ForumPost::getCreatedAt)
                .page(page);
    }
}
