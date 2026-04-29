package com.example.collegefreshmanhelper.admin.service.impl;

import com.example.collegefreshmanhelper.admin.dto.AdminKnowledgeCreateRequest;
import com.example.collegefreshmanhelper.admin.entity.KnowledgeQaTrace;
import com.example.collegefreshmanhelper.admin.service.AdminKnowledgeService;
import com.example.collegefreshmanhelper.admin.service.AdminOperationLogService;
import com.example.collegefreshmanhelper.admin.vo.AdminKnowledgeVO;
import com.example.collegefreshmanhelper.common.model.PageResult;
import com.example.collegefreshmanhelper.knowledge.service.KnowledgeTraceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminKnowledgeServiceImpl implements AdminKnowledgeService {

    private final KnowledgeTraceService knowledgeTraceService;
    private final AdminOperationLogService adminOperationLogService;

    @Override
    public PageResult<AdminKnowledgeVO> pageKnowledge(long pageNum, long pageSize, String keyword, Integer status, String category) {
        return knowledgeTraceService.pageKnowledge(pageNum, pageSize, keyword, status, category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeQaTrace createKnowledge(Long adminUserId, AdminKnowledgeCreateRequest request) {
        KnowledgeQaTrace trace = knowledgeTraceService.createManualKnowledge(adminUserId, request);
        adminOperationLogService.record(adminUserId, 4, trace.getId(), "MANUAL_KB_ADD", null, trace, "管理员手动录入知识库");
        return trace;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeQaTrace updateStatus(Long adminUserId, Long knowledgeId, Integer status, String reason) {
        KnowledgeQaTrace existing = knowledgeTraceService.getById(knowledgeId);
        KnowledgeQaTrace before = copy(existing);
        KnowledgeQaTrace trace = knowledgeTraceService.updateKnowledgeStatus(knowledgeId, status, reason);
        adminOperationLogService.record(adminUserId, 4, knowledgeId, "UPDATE_KB_STATUS", before, trace, reason);
        return trace;
    }

    private KnowledgeQaTrace copy(KnowledgeQaTrace source) {
        if (source == null) {
            return null;
        }
        KnowledgeQaTrace target = new KnowledgeQaTrace();
        target.setId(source.getId());
        target.setStatus(source.getStatus());
        target.setFailReason(source.getFailReason());
        target.setSyncedAt(source.getSyncedAt());
        return target;
    }
}
