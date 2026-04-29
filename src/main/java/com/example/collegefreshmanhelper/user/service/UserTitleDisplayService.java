package com.example.collegefreshmanhelper.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.collegefreshmanhelper.admin.entity.SysTitle;
import com.example.collegefreshmanhelper.admin.entity.SysUserTitle;
import com.example.collegefreshmanhelper.admin.mapper.SysTitleMapper;
import com.example.collegefreshmanhelper.admin.mapper.SysUserTitleMapper;
import com.example.collegefreshmanhelper.common.exception.BusinessException;
import com.example.collegefreshmanhelper.forum.entity.ForumReply;
import com.example.collegefreshmanhelper.forum.mapper.ForumReplyMapper;
import com.example.collegefreshmanhelper.user.entity.UserStats;
import com.example.collegefreshmanhelper.user.vo.UserTitleRuleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public static final int GRANT_SOURCE_AUTO = 1;
    public static final int GRANT_SOURCE_ADMIN = 2;

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

    @Transactional(rollbackFor = Exception.class)
    public void syncUserTitle(Long userId) {
        if (userId == null) {
            return;
        }
        syncUserTitles(Collections.singleton(userId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncUserTitles(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        List<Long> normalizedUserIds = userIds.stream().filter(Objects::nonNull).distinct().toList();
        if (normalizedUserIds.isEmpty()) {
            return;
        }

        Map<Long, SysUserTitle> currentMap = normalizeCurrentTitleMap(normalizedUserIds);
        Map<Long, UserTitleMetrics> metricsMap = buildMetricsMap(normalizedUserIds, Collections.emptyMap());
        List<SysTitle> automaticTitles = loadEnabledAutomaticTitles();

        for (Long userId : normalizedUserIds) {
            SysUserTitle current = currentMap.get(userId);
            if (current != null && isActiveAdminTitle(current)) {
                ensureWearing(current);
                continue;
            }

            if (current != null && isExpired(current)) {
                sysUserTitleMapper.deleteById(current.getId());
                current = null;
            }

            SysTitle automaticTitle = resolveAutomaticTitleEntity(metricsMap.get(userId), automaticTitles);
            if (automaticTitle == null) {
                if (current != null) {
                    sysUserTitleMapper.deleteById(current.getId());
                }
                continue;
            }

            if (current == null) {
                sysUserTitleMapper.insert(newAutoTitle(userId, automaticTitle.getId()));
                continue;
            }

            if (!Objects.equals(current.getTitleId(), automaticTitle.getId())
                    || !Integer.valueOf(1).equals(current.getIsWearing())
                    || !Integer.valueOf(GRANT_SOURCE_AUTO).equals(current.getGrantSource())
                    || current.getExpiredAt() != null) {
                current.setTitleId(automaticTitle.getId());
                current.setIsWearing(1);
                current.setGrantSource(GRANT_SOURCE_AUTO);
                current.setGrantRemark(null);
                current.setGrantedBy(null);
                current.setExpiredAt(null);
                current.setGrantedAt(LocalDateTime.now());
                sysUserTitleMapper.updateById(current);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public SysUserTitle grantAdminTitle(Long userId, Long titleId, Long adminUserId, String remark, LocalDateTime expiredAt) {
        SysTitle title = sysTitleMapper.selectById(titleId);
        if (title == null || !Integer.valueOf(1).equals(title.getStatus())) {
            throw new BusinessException("头衔不存在或已停用");
        }
        removeAllTitlesForUser(userId);

        SysUserTitle userTitle = new SysUserTitle();
        userTitle.setUserId(userId);
        userTitle.setTitleId(titleId);
        userTitle.setIsWearing(1);
        userTitle.setGrantSource(GRANT_SOURCE_ADMIN);
        userTitle.setGrantRemark(normalize(remark));
        userTitle.setGrantedBy(adminUserId);
        userTitle.setExpiredAt(expiredAt);
        sysUserTitleMapper.insert(userTitle);
        return userTitle;
    }

    @Transactional(rollbackFor = Exception.class)
    public void revokeTitleAndFallback(Long userTitleId) {
        SysUserTitle userTitle = sysUserTitleMapper.selectById(userTitleId);
        if (userTitle == null) {
            return;
        }
        Long userId = userTitle.getUserId();
        sysUserTitleMapper.deleteById(userTitleId);
        syncUserTitle(userId);
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
        SysTitle title = resolveAutomaticTitleEntity(metrics, loadEnabledAutomaticTitles());
        return title == null ? null : title.getTitleName();
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

    private SysTitle resolveAutomaticTitleEntity(UserTitleMetrics metrics, List<SysTitle> titles) {
        if (metrics == null || titles == null || titles.isEmpty()) {
            return null;
        }
        for (SysTitle title : titles) {
            if (matchesRule(title, metrics)) {
                return title;
            }
        }
        return null;
    }

    private Map<Long, SysUserTitle> normalizeCurrentTitleMap(Collection<Long> userIds) {
        List<SysUserTitle> records = sysUserTitleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserTitle>()
                        .in(SysUserTitle::getUserId, userIds)
                        .orderByDesc(SysUserTitle::getGrantSource)
                        .orderByDesc(SysUserTitle::getIsWearing)
                        .orderByDesc(SysUserTitle::getGrantedAt)
                        .orderByDesc(SysUserTitle::getId));
        if (records.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, SysUserTitle> currentMap = new LinkedHashMap<>();
        List<Long> duplicateIds = new ArrayList<>();
        for (SysUserTitle record : records) {
            if (currentMap.putIfAbsent(record.getUserId(), record) != null) {
                duplicateIds.add(record.getId());
            }
        }
        if (!duplicateIds.isEmpty()) {
            sysUserTitleMapper.deleteByIds(duplicateIds);
        }
        return currentMap;
    }

    private boolean isActiveAdminTitle(SysUserTitle userTitle) {
        return userTitle != null
                && Integer.valueOf(GRANT_SOURCE_ADMIN).equals(userTitle.getGrantSource())
                && !isExpired(userTitle);
    }

    private boolean isExpired(SysUserTitle userTitle) {
        return userTitle != null
                && userTitle.getExpiredAt() != null
                && !userTitle.getExpiredAt().isAfter(LocalDateTime.now());
    }

    private void ensureWearing(SysUserTitle userTitle) {
        if (Integer.valueOf(1).equals(userTitle.getIsWearing())) {
            return;
        }
        userTitle.setIsWearing(1);
        sysUserTitleMapper.updateById(userTitle);
    }

    private SysUserTitle newAutoTitle(Long userId, Long titleId) {
        SysUserTitle userTitle = new SysUserTitle();
        userTitle.setUserId(userId);
        userTitle.setTitleId(titleId);
        userTitle.setIsWearing(1);
        userTitle.setGrantSource(GRANT_SOURCE_AUTO);
        userTitle.setGrantRemark(null);
        userTitle.setGrantedBy(null);
        userTitle.setExpiredAt(null);
        return userTitle;
    }

    private void removeAllTitlesForUser(Long userId) {
        if (userId == null) {
            return;
        }
        sysUserTitleMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserTitle>()
                .eq(SysUserTitle::getUserId, userId));
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

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
