package com.example.collegefreshmanhelper.admin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.collegefreshmanhelper.admin.dto.AdminBanUserRequest;
import com.example.collegefreshmanhelper.admin.dto.AdminCreateAdminRequest;
import com.example.collegefreshmanhelper.admin.service.AdminOperationLogService;
import com.example.collegefreshmanhelper.admin.service.AdminUserService;
import com.example.collegefreshmanhelper.admin.vo.AdminUserDetailVO;
import com.example.collegefreshmanhelper.admin.vo.AdminUserSummaryVO;
import com.example.collegefreshmanhelper.common.model.PageResult;
import com.example.collegefreshmanhelper.common.exception.BusinessException;
import com.example.collegefreshmanhelper.user.entity.SysUser;
import com.example.collegefreshmanhelper.user.entity.UserStats;
import com.example.collegefreshmanhelper.user.service.UserService;
import com.example.collegefreshmanhelper.user.service.UserStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserService userService;
    private final UserStatsService userStatsService;
    private final AdminOperationLogService adminOperationLogService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<AdminUserSummaryVO> pageUsers(long pageNum, long pageSize, String keyword, Integer role, Integer status) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        var query = userService.lambdaQuery()
                .eq(SysUser::getDeleted, 0);
        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();
            query.and(wrapper -> {
                wrapper.like(SysUser::getUsername, trimmedKeyword)
                        .or()
                        .like(SysUser::getNickname, trimmedKeyword);
                if (trimmedKeyword.chars().allMatch(Character::isDigit)) {
                    wrapper.or().eq(SysUser::getId, Long.valueOf(trimmedKeyword));
                }
            });
        }
        if (role != null) {
            query.eq(SysUser::getRole, role);
        }
        if (status != null) {
            query.eq(SysUser::getStatus, status);
        }
        query.orderByDesc(SysUser::getCreatedAt).page(page);

        Map<Long, UserStats> statsMap = loadStatsMap(page.getRecords().stream().map(SysUser::getId).collect(Collectors.toSet()));
        Page<AdminUserSummaryVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(page.getRecords().stream().map(user -> toSummaryVO(user, statsMap.get(user.getId()))).toList());
        return PageResult.of(resultPage);
    }

    @Override
    public AdminUserDetailVO getUserDetail(Long userId) {
        SysUser user = userService.getVisibleById(userId);
        UserStats stats = userStatsService.getById(userId);
        return toDetailVO(user, stats);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser createAdminUser(Long adminUserId, AdminCreateAdminRequest request) {
        if (userService.findByUsername(request.getUsername().trim()) != null) {
            throw new BusinessException("管理员账号已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname().trim());
        user.setGender(0);
        user.setRole(8);
        user.setPoints(0);
        user.setStatus(1);
        user.setDeleted(0);
        userService.save(user);

        UserStats stats = new UserStats();
        stats.setUserId(user.getId());
        userStatsService.save(stats);
        adminOperationLogService.record(adminUserId, 1, user.getId(), "CREATE_ADMIN_USER", null, copyUser(user), "创建管理员账号");
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser updateAdminStatus(Long adminUserId, Long targetUserId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态仅支持 0禁用 或 1启用");
        }
        SysUser user = userService.getVisibleById(targetUserId);
        if (!Integer.valueOf(8).equals(user.getRole())) {
            throw new BusinessException("只能管理普通管理员账号");
        }
        SysUser before = copyUser(user);
        user.setStatus(status);
        if (status == 1) {
            user.setBanReason(null);
            user.setBanExpireAt(null);
        }
        userService.updateById(user);
        adminOperationLogService.record(adminUserId, 1, targetUserId, status == 1 ? "ENABLE_ADMIN_USER" : "DISABLE_ADMIN_USER", before, user, null);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser banUser(Long adminUserId, Long targetUserId, AdminBanUserRequest request) {
        SysUser admin = userService.getVisibleById(adminUserId);
        SysUser user = userService.getVisibleById(targetUserId);
        if (Integer.valueOf(9).equals(user.getRole())) {
            throw new BusinessException("不能封禁超级管理员");
        }
        if (Integer.valueOf(8).equals(admin.getRole()) && user.getRole() != null && user.getRole() >= 8) {
            throw new BusinessException("普通管理员不能封禁管理员账号");
        }

        SysUser before = copyUser(user);
        user.setStatus(0);
        user.setBanReason(request.getReason());
        user.setBanExpireAt(request.getBanExpireAt());
        userService.updateById(user);

        adminOperationLogService.record(adminUserId, 1, targetUserId, "BAN_USER", before, user, request.getReason());
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser unbanUser(Long adminUserId, Long targetUserId) {
        SysUser admin = userService.getVisibleById(adminUserId);
        SysUser user = userService.getVisibleById(targetUserId);
        if (Integer.valueOf(8).equals(admin.getRole()) && user.getRole() != null && user.getRole() >= 8) {
            throw new BusinessException("普通管理员不能解封管理员账号");
        }
        SysUser before = copyUser(user);
        user.setStatus(1);
        user.setBanReason(null);
        user.setBanExpireAt(null);
        userService.updateById(user);

        adminOperationLogService.record(adminUserId, 1, targetUserId, "UNBAN_USER", before, user, null);
        return user;
    }

    private Map<Long, UserStats> loadStatsMap(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userStatsService.listByIds(userIds).stream()
                .collect(Collectors.toMap(UserStats::getUserId, Function.identity()));
    }

    private AdminUserSummaryVO toSummaryVO(SysUser user, UserStats stats) {
        AdminUserSummaryVO vo = new AdminUserSummaryVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setPoints(defaultZero(user.getPoints()));
        vo.setBanExpireAt(user.getBanExpireAt());
        vo.setPostCount(stats == null ? 0 : defaultZero(stats.getPostCount()));
        vo.setReplyCount(stats == null ? 0 : defaultZero(stats.getReplyCount()));
        vo.setKnowledgeContributionCount(stats == null ? 0 : defaultZero(stats.getKnowledgeContributionCount()));
        vo.setFeaturedAnswerCount(stats == null ? 0 : defaultZero(stats.getFeaturedAnswerCount()));
        vo.setTotalLikeReceivedCount(stats == null ? 0 : defaultZero(stats.getPostLikeReceivedCount()) + defaultZero(stats.getReplyLikeReceivedCount()));
        vo.setLastLoginAt(user.getLastLoginAt());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }

    private AdminUserDetailVO toDetailVO(SysUser user, UserStats stats) {
        AdminUserDetailVO vo = new AdminUserDetailVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setGender(user.getGender());
        vo.setAdmissionYear(user.getAdmissionYear());
        vo.setCollegeName(user.getCollegeName());
        vo.setMajorName(user.getMajorName());
        vo.setBio(user.getBio());
        vo.setPoints(defaultZero(user.getPoints()));
        vo.setBanExpireAt(user.getBanExpireAt());
        vo.setBanReason(user.getBanReason());
        vo.setLastLoginAt(user.getLastLoginAt());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setPostCount(stats == null ? 0 : defaultZero(stats.getPostCount()));
        vo.setReplyCount(stats == null ? 0 : defaultZero(stats.getReplyCount()));
        vo.setPostLikeReceivedCount(stats == null ? 0 : defaultZero(stats.getPostLikeReceivedCount()));
        vo.setReplyLikeReceivedCount(stats == null ? 0 : defaultZero(stats.getReplyLikeReceivedCount()));
        vo.setKnowledgeContributionCount(stats == null ? 0 : defaultZero(stats.getKnowledgeContributionCount()));
        vo.setFeaturedAnswerCount(stats == null ? 0 : defaultZero(stats.getFeaturedAnswerCount()));
        return vo;
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private SysUser copyUser(SysUser source) {
        SysUser target = new SysUser();
        target.setId(source.getId());
        target.setUsername(source.getUsername());
        target.setNickname(source.getNickname());
        target.setRole(source.getRole());
        target.setStatus(source.getStatus());
        target.setBanReason(source.getBanReason());
        target.setBanExpireAt(source.getBanExpireAt());
        target.setDeleted(source.getDeleted());
        return target;
    }
}
