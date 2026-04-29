package com.example.collegefreshmanhelper.knowledge.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.collegefreshmanhelper.admin.dto.AdminKnowledgeCreateRequest;
import com.example.collegefreshmanhelper.admin.entity.KnowledgeQaTrace;
import com.example.collegefreshmanhelper.admin.mapper.KnowledgeQaTraceMapper;
import com.example.collegefreshmanhelper.admin.vo.AdminKnowledgeVO;
import com.example.collegefreshmanhelper.common.exception.BusinessException;
import com.example.collegefreshmanhelper.common.model.PageResult;
import com.example.collegefreshmanhelper.knowledge.service.KnowledgeTraceService;
import com.example.collegefreshmanhelper.user.entity.SysUser;
import com.example.collegefreshmanhelper.user.service.UserService;
import com.example.collegefreshmanhelper.user.service.UserStatsSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeTraceServiceImpl implements KnowledgeTraceService {

    private final KnowledgeQaTraceMapper knowledgeQaTraceMapper;
    private final UserService userService;
    private final UserStatsSyncService userStatsSyncService;

    @Override
    public KnowledgeQaTrace getById(Long knowledgeId) {
        return knowledgeId == null ? null : knowledgeQaTraceMapper.selectById(knowledgeId);
    }

    @Override
    public List<KnowledgeQaTrace> listEnabledKnowledge() {
        return knowledgeQaTraceMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KnowledgeQaTrace>()
                .eq(KnowledgeQaTrace::getStatus, 2)
                .isNotNull(KnowledgeQaTrace::getQuestionText)
                .isNotNull(KnowledgeQaTrace::getAnswerText)
                .orderByDesc(KnowledgeQaTrace::getCreatedAt));
    }

    @Override
    public PageResult<AdminKnowledgeVO> pageKnowledge(long pageNum, long pageSize, String keyword, Integer status, String category) {
        Page<KnowledgeQaTrace> page = new Page<>(pageNum, pageSize);
        Page<KnowledgeQaTrace> query = knowledgeQaTraceMapper.selectPage(page,
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KnowledgeQaTrace>()
                        .and(StringUtils.hasText(keyword), wrapper -> wrapper
                                .like(KnowledgeQaTrace::getQuestionText, keyword.trim())
                                .or()
                                .like(KnowledgeQaTrace::getAnswerText, keyword.trim()))
                        .eq(status != null, KnowledgeQaTrace::getStatus, status)
                        .eq(StringUtils.hasText(category), KnowledgeQaTrace::getCategory, category == null ? null : category.trim())
                        .orderByDesc(KnowledgeQaTrace::getCreatedAt));

        Set<Long> userIds = query.getRecords().stream()
                .flatMap(item -> java.util.stream.Stream.of(item.getContributorUserId(), item.getCreatedByAdminId()))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, SysUser> userMap = loadUserMap(userIds);

        Page<AdminKnowledgeVO> resultPage = new Page<>(query.getCurrent(), query.getSize(), query.getTotal());
        resultPage.setRecords(query.getRecords().stream().map(item -> toVO(item, userMap)).toList());
        return PageResult.of(resultPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeQaTrace createManualKnowledge(Long adminUserId, AdminKnowledgeCreateRequest request) {
        KnowledgeQaTrace trace = new KnowledgeQaTrace();
        trace.setSourceType(2);
        trace.setCreatedByAdminId(adminUserId);
        trace.setQuestionText(request.getQuestionText().trim());
        trace.setAnswerText(request.getAnswerText().trim());
        trace.setCategory(normalize(request.getCategory()));
        trace.setCollectionName(StringUtils.hasText(request.getCollectionName()) ? request.getCollectionName().trim() : "freshman_qa");
        trace.setQdrantPointId("manual_" + UUID.randomUUID().toString().replace("-", ""));
        trace.setVectorDimension(1536);
        trace.setEmbeddingModel(StringUtils.hasText(request.getEmbeddingModel()) ? request.getEmbeddingModel().trim() : "text-embedding-v2");
        trace.setLlmModel(StringUtils.hasText(request.getLlmModel()) ? request.getLlmModel().trim() : "qwen-plus");
        trace.setLikeCountSnapshot(0);
        trace.setRewardPoints(Math.max(request.getRewardPoints() == null ? 0 : request.getRewardPoints(), 0));
        trace.setStatus(1);
        trace.setContextText(buildContext(trace.getQuestionText(), trace.getAnswerText()));
        knowledgeQaTraceMapper.insert(trace);
        syncContributorStats(trace.getContributorUserId());
        return trace;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeQaTrace updateKnowledgeStatus(Long knowledgeId, Integer status, String reason) {
        validateStatus(status);
        KnowledgeQaTrace trace = knowledgeQaTraceMapper.selectById(knowledgeId);
        if (trace == null) {
            throw new BusinessException("知识条目不存在");
        }
        trace.setStatus(status);
        if (status.intValue() == 4) {
            trace.setFailReason(normalize(reason));
        } else if (status.intValue() == 2) {
            trace.setFailReason(null);
        }
        knowledgeQaTraceMapper.updateById(trace);
        syncContributorStats(trace.getContributorUserId());
        return trace;
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 1 && status != 2 && status != 4)) {
            throw new BusinessException("知识条目状态仅支持 1待向量化 2已启用 4已下线");
        }
    }

    private Map<Long, SysUser> loadUserMap(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
    }

    private AdminKnowledgeVO toVO(KnowledgeQaTrace trace, Map<Long, SysUser> userMap) {
        AdminKnowledgeVO vo = new AdminKnowledgeVO();
        vo.setId(trace.getId());
        vo.setSourceType(trace.getSourceType());
        vo.setSourcePostId(trace.getSourcePostId());
        vo.setSourceReplyId(trace.getSourceReplyId());
        vo.setContributorUserId(trace.getContributorUserId());
        vo.setContributorNickname(resolveNickname(userMap.get(trace.getContributorUserId())));
        vo.setCreatedByAdminId(trace.getCreatedByAdminId());
        vo.setCreatedByAdminNickname(resolveNickname(userMap.get(trace.getCreatedByAdminId())));
        vo.setQuestionText(trace.getQuestionText());
        vo.setAnswerText(trace.getAnswerText());
        vo.setCategory(trace.getCategory());
        vo.setCollectionName(trace.getCollectionName());
        vo.setQdrantPointId(trace.getQdrantPointId());
        vo.setStatus(trace.getStatus());
        vo.setLikeCountSnapshot(defaultZero(trace.getLikeCountSnapshot()));
        vo.setRewardPoints(defaultZero(trace.getRewardPoints()));
        vo.setFailReason(trace.getFailReason());
        vo.setSyncedAt(trace.getSyncedAt());
        vo.setCreatedAt(trace.getCreatedAt());
        return vo;
    }

    private String resolveNickname(SysUser user) {
        return user == null ? null : user.getNickname();
    }

    private String buildContext(String questionText, String answerText) {
        return "Q: " + questionText + "\nA: " + answerText;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private void syncContributorStats(Long contributorUserId) {
        if (contributorUserId == null) {
            return;
        }
        userStatsSyncService.syncKnowledgeStatsForUsers(Collections.singleton(contributorUserId));
    }
}
