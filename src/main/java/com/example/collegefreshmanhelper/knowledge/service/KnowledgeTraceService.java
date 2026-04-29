package com.example.collegefreshmanhelper.knowledge.service;

import com.example.collegefreshmanhelper.admin.dto.AdminKnowledgeCreateRequest;
import com.example.collegefreshmanhelper.admin.entity.KnowledgeQaTrace;
import com.example.collegefreshmanhelper.admin.vo.AdminKnowledgeVO;
import com.example.collegefreshmanhelper.common.model.PageResult;

public interface KnowledgeTraceService {

    KnowledgeQaTrace getById(Long knowledgeId);

    PageResult<AdminKnowledgeVO> pageKnowledge(long pageNum, long pageSize, String keyword, Integer status, String category);

    KnowledgeQaTrace createManualKnowledge(Long adminUserId, AdminKnowledgeCreateRequest request);

    KnowledgeQaTrace updateKnowledgeStatus(Long knowledgeId, Integer status, String reason);
}
