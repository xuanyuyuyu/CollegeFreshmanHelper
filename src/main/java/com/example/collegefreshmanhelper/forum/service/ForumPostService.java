package com.example.collegefreshmanhelper.forum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.collegefreshmanhelper.forum.dto.ForumPostCreateRequest;
import com.example.collegefreshmanhelper.forum.entity.ForumPost;
import com.example.collegefreshmanhelper.forum.vo.ForumPostDetailVO;

public interface ForumPostService extends IService<ForumPost> {

    ForumPost createPost(Long currentUserId, ForumPostCreateRequest request);

    ForumPostDetailVO getPostDetail(Long postId);

    Page<ForumPost> pagePublishedPosts(long pageNum, long pageSize);
}
