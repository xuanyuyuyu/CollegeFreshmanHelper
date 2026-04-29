package com.example.collegefreshmanhelper.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.collegefreshmanhelper.admin.entity.KnowledgeQaTrace;
import com.example.collegefreshmanhelper.admin.mapper.KnowledgeQaTraceMapper;
import com.example.collegefreshmanhelper.user.entity.UserStats;
import com.example.collegefreshmanhelper.user.mapper.UserStatsMapper;
import com.example.collegefreshmanhelper.user.service.UserStatsSyncService;
import com.example.collegefreshmanhelper.user.service.UserTitleDisplayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserStatsSyncServiceImpl implements UserStatsSyncService {

    private final KnowledgeQaTraceMapper knowledgeQaTraceMapper;
    private final UserStatsMapper userStatsMapper;
    private final UserTitleDisplayService userTitleDisplayService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncKnowledgeStatsForUsers(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        List<Long> normalizedUserIds = userIds.stream().filter(Objects::nonNull).distinct().toList();
        if (normalizedUserIds.isEmpty()) {
            return;
        }

        Map<Long, UserStats> statsMap = loadOrInitStats(normalizedUserIds);
        Map<Long, Integer> contributionMap = loadContributionCountMap(normalizedUserIds);
        Map<Long, Integer> featuredMap = loadFeaturedAnswerCountMap(normalizedUserIds);

        for (Long userId : normalizedUserIds) {
            UserStats stats = statsMap.get(userId);
            stats.setKnowledgeContributionCount(contributionMap.getOrDefault(userId, 0));
            stats.setFeaturedAnswerCount(featuredMap.getOrDefault(userId, 0));
            userStatsMapper.updateById(stats);
        }
        userTitleDisplayService.syncUserTitles(normalizedUserIds);
    }

    private Map<Long, UserStats> loadOrInitStats(Collection<Long> userIds) {
        Map<Long, UserStats> result = new LinkedHashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return result;
        }
        for (UserStats stats : userStatsMapper.selectBatchIds(userIds)) {
            fillDefaults(stats);
            result.put(stats.getUserId(), stats);
        }
        for (Long userId : userIds) {
            result.computeIfAbsent(userId, key -> {
                UserStats stats = new UserStats();
                stats.setUserId(userId);
                fillDefaults(stats);
                userStatsMapper.insert(stats);
                return stats;
            });
        }
        return result;
    }

    private Map<Long, Integer> loadContributionCountMap(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> rows = knowledgeQaTraceMapper.selectMaps(new QueryWrapper<KnowledgeQaTrace>()
                .select("contributor_user_id AS contributorUserId", "COUNT(*) AS totalCount")
                .in("contributor_user_id", userIds)
                .ne("status", 4)
                .groupBy("contributor_user_id"));
        return toCountMap(rows, "contributorUserId", "totalCount");
    }

    private Map<Long, Integer> loadFeaturedAnswerCountMap(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> rows = knowledgeQaTraceMapper.selectMaps(new QueryWrapper<KnowledgeQaTrace>()
                .select("contributor_user_id AS contributorUserId", "COUNT(*) AS totalCount")
                .in("contributor_user_id", userIds)
                .isNotNull("source_reply_id")
                .ne("status", 4)
                .groupBy("contributor_user_id"));
        return toCountMap(rows, "contributorUserId", "totalCount");
    }

    private Map<Long, Integer> toCountMap(List<Map<String, Object>> rows, String userKey, String countKey) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Integer> result = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Long userId = toLong(row.get(userKey));
            if (userId == null) {
                continue;
            }
            result.put(userId, toInt(row.get(countKey)));
        }
        return result;
    }

    private void fillDefaults(UserStats stats) {
        if (stats.getPostCount() == null) stats.setPostCount(0);
        if (stats.getReplyCount() == null) stats.setReplyCount(0);
        if (stats.getReplyLikeReceivedCount() == null) stats.setReplyLikeReceivedCount(0);
        if (stats.getPostLikeReceivedCount() == null) stats.setPostLikeReceivedCount(0);
        if (stats.getKnowledgeContributionCount() == null) stats.setKnowledgeContributionCount(0);
        if (stats.getFeaturedAnswerCount() == null) stats.setFeaturedAnswerCount(0);
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }

    private int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return value == null ? 0 : Integer.parseInt(String.valueOf(value));
    }
}
