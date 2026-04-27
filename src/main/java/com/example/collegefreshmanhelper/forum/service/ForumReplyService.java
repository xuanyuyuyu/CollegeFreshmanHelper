package com.example.collegefreshmanhelper.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.collegefreshmanhelper.forum.dto.ForumReplyCreateRequest;
import com.example.collegefreshmanhelper.forum.entity.ForumReply;
import com.example.collegefreshmanhelper.forum.vo.ForumReplyThreadVO;

import java.util.List;

public interface ForumReplyService extends IService<ForumReply> {

    ForumReply createReply(Long postId, Long currentUserId, ForumReplyCreateRequest request);

    List<ForumReplyThreadVO> listPublishedRepliesByPostId(Long postId);
}
