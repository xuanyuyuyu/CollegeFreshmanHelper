package com.example.collegefreshmanhelper.admin.service;

import com.example.collegefreshmanhelper.admin.dto.AdminKnowledgeCreateRequest;
import com.example.collegefreshmanhelper.admin.entity.KnowledgeQaTrace;
import com.example.collegefreshmanhelper.admin.vo.AdminKnowledgeVO;
import com.example.collegefreshmanhelper.common.model.PageResult;

public interface AdminKnowledgeService {

    PageResult<AdminKnowledgeVO> pageKnowledge(long pageNum, long pageSize, String keyword, Integer status, String category);

    KnowledgeQaTrace createKnowledge(Long adminUserId, AdminKnowledgeCreateRequest request);

    KnowledgeQaTrace updateStatus(Long adminUserId, Long knowledgeId, Integer status, String reason);
}
