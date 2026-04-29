package com.example.collegefreshmanhelper.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.collegefreshmanhelper.admin.entity.SysTitle;
import com.example.collegefreshmanhelper.admin.entity.SysUserTitle;
import com.example.collegefreshmanhelper.admin.mapper.SysTitleMapper;
import com.example.collegefreshmanhelper.admin.mapper.SysUserTitleMapper;
import com.example.collegefreshmanhelper.forum.entity.ForumReply;
import com.example.collegefreshmanhelper.forum.mapper.ForumReplyMapper;
import com.example.collegefreshmanhelper.user.entity.UserStats;
import com.example.collegefreshmanhelper.user.vo.UserTitleRuleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserTitleDisplayService {

    private final SysUserTitleMapper sysUserTitleMapper;
    private final SysTitleMapper sysTitleMapper;
    private final ForumReplyMapper forumReplyMapper;

    public String findWearingTitle(Long userId) {
        if (userId == null) {
            return null;
        }
        return findWearingTitles(Collections.singleton(userId)).get(userId);
    }

    public Map<Long, String> findWearingTitles(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDateTime now = LocalDateTime.now();
        java.util.List<SysUserTitle> userTitles = sysUserTitleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserTitle>()
                        .in(SysUserTitle::getUserId, userIds)
                        .eq(SysUserTitle::getIsWearing, 1)
                        .and(wrapper -> wrapper.isNull(SysUserTitle::getExpiredAt).or().gt(SysUserTitle::getExpiredAt, now))
                        .orderByDesc(SysUserTitle::getGrantedAt)
                        .orderByDesc(SysUserTitle::getId));
        if (userTitles.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> titleIds = userTitles.stream()
                .map(SysUserTitle::getTitleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (titleIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, SysTitle> titleMap = sysTitleMapper.selectBatchIds(titleIds).stream()
                .filter(title -> Integer.valueOf(1).equals(title.getStatus()))
                .collect(Collectors.toMap(SysTitle::getId, title -> title));

        Map<Long, String> result = new LinkedHashMap<>();
        for (SysUserTitle userTitle : userTitles) {
            SysTitle title = titleMap.get(userTitle.getTitleId());
            if (title == null) {
                continue;
            }
            result.putIfAbsent(userTitle.getUserId(), title.getTitleName());
        }
        return result;
    }

    public List<UserTitleRuleVO> listAutomaticTitleRules() {
        return loadEnabledAutomaticTitles().stream()
                .map(this::toRuleVO)
                .toList();
    }

    public String resolveAutomaticTitle(UserTitleMetrics metrics) {
        if (metrics == null) {
            return null;
        }
        for (SysTitle title : loadEnabledAutomaticTitles()) {
            if (matchesRule(title, metrics)) {
                return title.getTitleName();
            }
        }
        return null;
    }

    public Map<Long, UserTitleMetrics> buildMetricsMap(Collection<Long> userIds, Map<Long, UserStats> statsMap) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, UserTitleMetrics> result = new LinkedHashMap<>();
        for (Long userId : userIds) {
            UserStats stats = statsMap == null ? null : statsMap.get(userId);
            result.put(userId, new UserTitleMetrics(
                    0,
                    0,
                    defaultZero(stats == null ? null : stats.getKnowledgeContributionCount()),
                    defaultZero(stats == null ? null : stats.getFeaturedAnswerCount())
            ));
        }

        List<Map<String, Object>> replyStats = forumReplyMapper.selectMaps(new QueryWrapper<ForumReply>()
                .select("user_id AS userId", "COUNT(*) AS replyCount", "COALESCE(SUM(like_count), 0) AS replyLikeReceivedCount")
                .in("user_id", userIds)
                .eq("deleted", 0)
                .groupBy("user_id"));
        for (Map<String, Object> row : replyStats) {
            Long userId = toLong(row.get("userId"));
            if (userId == null) {
                continue;
            }
            UserTitleMetrics current = result.get(userId);
            if (current == null) {
                current = new UserTitleMetrics(0, 0, 0, 0);
            }
            result.put(userId, new UserTitleMetrics(
                    toInt(row.get("replyCount")),
                    toInt(row.get("replyLikeReceivedCount")),
                    current.knowledgeContributionCount(),
                    current.featuredAnswerCount()
            ));
        }
        return result;
    }

    public UserTitleMetrics buildMetrics(UserStats stats, int replyCount, int replyLikeReceivedCount) {
        return new UserTitleMetrics(
                replyCount,
                replyLikeReceivedCount,
                defaultZero(stats == null ? null : stats.getKnowledgeContributionCount()),
                defaultZero(stats == null ? null : stats.getFeaturedAnswerCount())
        );
    }

    private List<SysTitle> loadEnabledAutomaticTitles() {
        return sysTitleMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysTitle>()
                        .eq(SysTitle::getStatus, 1)
                        .eq(SysTitle::getGrantType, 1))
                .stream()
                .sorted(Comparator.comparing(SysTitle::getSortNo, Comparator.nullsLast(Integer::compareTo)).reversed()
                        .thenComparing(SysTitle::getId, Comparator.nullsLast(Long::compareTo)).reversed())
                .toList();
    }

    private boolean matchesRule(SysTitle title, UserTitleMetrics metrics) {
        Integer ruleType = title.getRuleType();
        Integer threshold = title.getRuleThreshold();
        if (ruleType == null || threshold == null) {
            return false;
        }
        return switch (ruleType) {
            case 1 -> metrics.replyCount() >= threshold;
            case 2 -> metrics.replyLikeReceivedCount() >= threshold;
            case 3 -> metrics.knowledgeContributionCount() >= threshold;
            case 4 -> metrics.featuredAnswerCount() >= threshold;
            default -> false;
        };
    }

    private UserTitleRuleVO toRuleVO(SysTitle title) {
        UserTitleRuleVO vo = new UserTitleRuleVO();
        vo.setId(title.getId());
        vo.setTitleName(title.getTitleName());
        vo.setTitleCode(title.getTitleCode());
        vo.setDescription(title.getDescription());
        vo.setRuleType(title.getRuleType());
        vo.setRuleThreshold(title.getRuleThreshold());
        vo.setSortNo(title.getSortNo());
        return vo;
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return value == null ? 0 : Integer.parseInt(String.valueOf(value));
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }
}
