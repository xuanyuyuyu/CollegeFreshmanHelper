package com.example.collegefreshmanhelper.forum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.collegefreshmanhelper.forum.dto.ForumPostCreateRequest;
import com.example.collegefreshmanhelper.forum.entity.ForumPost;
import com.example.collegefreshmanhelper.forum.vo.ForumPostDetailVO;
import com.example.collegefreshmanhelper.forum.vo.ForumPostSummaryVO;

public interface ForumPostService extends IService<ForumPost> {

    ForumPost createPost(Long currentUserId, ForumPostCreateRequest request);

    void deleteOwnPost(Long currentUserId, Long postId);

    ForumPostDetailVO getPostDetail(Long postId, boolean incrementView, Long currentUserId);

    Page<ForumPostSummaryVO> pagePublishedPosts(long pageNum, long pageSize, String sortType, Long currentUserId);
}
