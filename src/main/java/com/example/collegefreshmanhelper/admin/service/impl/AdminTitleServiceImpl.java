package com.example.collegefreshmanhelper.admin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.collegefreshmanhelper.admin.dto.AdminGrantTitleRequest;
import com.example.collegefreshmanhelper.admin.entity.SysTitle;
import com.example.collegefreshmanhelper.admin.entity.SysUserTitle;
import com.example.collegefreshmanhelper.admin.mapper.SysTitleMapper;
import com.example.collegefreshmanhelper.admin.mapper.SysUserTitleMapper;
import com.example.collegefreshmanhelper.admin.service.AdminOperationLogService;
import com.example.collegefreshmanhelper.admin.service.AdminTitleService;
import com.example.collegefreshmanhelper.admin.vo.AdminTitleVO;
import com.example.collegefreshmanhelper.admin.vo.AdminUserTitleVO;
import com.example.collegefreshmanhelper.common.exception.BusinessException;
import com.example.collegefreshmanhelper.common.model.PageResult;
import com.example.collegefreshmanhelper.user.entity.SysUser;
import com.example.collegefreshmanhelper.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminTitleServiceImpl implements AdminTitleService {

    private final SysTitleMapper sysTitleMapper;
    private final SysUserTitleMapper sysUserTitleMapper;
    private final UserService userService;
    private final AdminOperationLogService adminOperationLogService;

    @Override
    public List<AdminTitleVO> listEnabledTitles() {
        return sysTitleMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysTitle>()
                        .eq(SysTitle::getStatus, 1)
                        .orderByAsc(SysTitle::getSortNo)
                        .orderByAsc(SysTitle::getId))
                .stream()
                .map(this::toTitleVO)
                .toList();
    }

    @Override
    public PageResult<AdminUserTitleVO> pageUserTitles(long pageNum, long pageSize, Long userId) {
        Page<SysUserTitle> page = new Page<>(pageNum, pageSize);
        Page<SysUserTitle> query = sysUserTitleMapper.selectPage(page,
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserTitle>()
                        .eq(userId != null, SysUserTitle::getUserId, userId)
                        .orderByDesc(SysUserTitle::getGrantedAt));
        Set<Long> userIds = query.getRecords().stream()
                .flatMap(item -> java.util.stream.Stream.of(item.getUserId(), item.getGrantedBy()))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> titleIds = query.getRecords().stream().map(SysUserTitle::getTitleId).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = loadUserMap(userIds);
        Map<Long, SysTitle> titleMap = loadTitleMap(titleIds);

        Page<AdminUserTitleVO> resultPage = new Page<>(query.getCurrent(), query.getSize(), query.getTotal());
        resultPage.setRecords(query.getRecords().stream().map(item -> toUserTitleVO(item, userMap, titleMap)).toList());
        return PageResult.of(resultPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUserTitle grantTitle(Long adminUserId, AdminGrantTitleRequest request) {
        userService.getVisibleById(request.getUserId());
        SysTitle title = getEnabledTitle(request.getTitleId());
        SysUserTitle exists = sysUserTitleMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserTitle>()
                .eq(SysUserTitle::getUserId, request.getUserId())
                .eq(SysUserTitle::getTitleId, request.getTitleId()));
        if (exists != null) {
            throw new BusinessException("该用户已拥有此头衔");
        }

        boolean wearing = Boolean.TRUE.equals(request.getWearing());
        if (wearing) {
            clearWearingForUser(request.getUserId());
        }

        SysUserTitle userTitle = new SysUserTitle();
        userTitle.setUserId(request.getUserId());
        userTitle.setTitleId(title.getId());
        userTitle.setIsWearing(wearing ? 1 : 0);
        userTitle.setGrantSource(2);
        userTitle.setGrantRemark(normalize(request.getRemark()));
        userTitle.setGrantedBy(adminUserId);
        userTitle.setExpiredAt(request.getExpiredAt());
        sysUserTitleMapper.insert(userTitle);
        adminOperationLogService.record(adminUserId, 6, userTitle.getId(), "GRANT_TITLE", null, userTitle, request.getRemark());
        return userTitle;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeTitle(Long adminUserId, Long userTitleId, String reason) {
        SysUserTitle userTitle = sysUserTitleMapper.selectById(userTitleId);
        if (userTitle == null) {
            throw new BusinessException("用户头衔记录不存在");
        }
        SysUserTitle before = copy(userTitle);
        sysUserTitleMapper.deleteById(userTitleId);
        adminOperationLogService.record(adminUserId, 6, userTitleId, "REVOKE_TITLE", before, null, reason);
    }

    private SysTitle getEnabledTitle(Long titleId) {
        SysTitle title = sysTitleMapper.selectById(titleId);
        if (title == null || !Integer.valueOf(1).equals(title.getStatus())) {
            throw new BusinessException("头衔不存在或已停用");
        }
        return title;
    }

    private void clearWearingForUser(Long userId) {
        List<SysUserTitle> wearingTitles = sysUserTitleMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserTitle>()
                .eq(SysUserTitle::getUserId, userId)
                .eq(SysUserTitle::getIsWearing, 1));
        if (wearingTitles.isEmpty()) {
            return;
        }
        for (SysUserTitle item : wearingTitles) {
            item.setIsWearing(0);
            sysUserTitleMapper.updateById(item);
        }
    }

    private Map<Long, SysUser> loadUserMap(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userService.listByIds(userIds).stream().collect(Collectors.toMap(SysUser::getId, Function.identity()));
    }

    private Map<Long, SysTitle> loadTitleMap(Set<Long> titleIds) {
        if (titleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysTitleMapper.selectBatchIds(titleIds).stream().collect(Collectors.toMap(SysTitle::getId, Function.identity()));
    }

    private AdminTitleVO toTitleVO(SysTitle title) {
        AdminTitleVO vo = new AdminTitleVO();
        vo.setId(title.getId());
        vo.setTitleName(title.getTitleName());
        vo.setTitleCode(title.getTitleCode());
        vo.setDescription(title.getDescription());
        vo.setGrantType(title.getGrantType());
        vo.setRuleType(title.getRuleType());
        vo.setRuleThreshold(title.getRuleThreshold());
        vo.setSortNo(title.getSortNo());
        vo.setStatus(title.getStatus());
        return vo;
    }

    private AdminUserTitleVO toUserTitleVO(SysUserTitle item, Map<Long, SysUser> userMap, Map<Long, SysTitle> titleMap) {
        AdminUserTitleVO vo = new AdminUserTitleVO();
        vo.setId(item.getId());
        vo.setUserId(item.getUserId());
        vo.setUserNickname(resolveNickname(userMap.get(item.getUserId())));
        vo.setTitleId(item.getTitleId());
        vo.setTitleName(resolveTitleName(titleMap.get(item.getTitleId())));
        vo.setIsWearing(item.getIsWearing());
        vo.setGrantSource(item.getGrantSource());
        vo.setGrantRemark(item.getGrantRemark());
        vo.setGrantedBy(item.getGrantedBy());
        vo.setGrantedByNickname(resolveNickname(userMap.get(item.getGrantedBy())));
        vo.setGrantedAt(item.getGrantedAt());
        vo.setExpiredAt(item.getExpiredAt());
        return vo;
    }

    private String resolveNickname(SysUser user) {
        return user == null ? null : user.getNickname();
    }

    private String resolveTitleName(SysTitle title) {
        return title == null ? null : title.getTitleName();
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private SysUserTitle copy(SysUserTitle source) {
        SysUserTitle target = new SysUserTitle();
        target.setId(source.getId());
        target.setUserId(source.getUserId());
        target.setTitleId(source.getTitleId());
        target.setIsWearing(source.getIsWearing());
        target.setGrantSource(source.getGrantSource());
        target.setGrantRemark(source.getGrantRemark());
        target.setGrantedBy(source.getGrantedBy());
        target.setGrantedAt(source.getGrantedAt());
        target.setExpiredAt(source.getExpiredAt());
        return target;
    }
}
