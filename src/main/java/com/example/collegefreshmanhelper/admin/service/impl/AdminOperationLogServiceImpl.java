package com.example.collegefreshmanhelper.admin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.collegefreshmanhelper.admin.entity.AdminOperationLog;
import com.example.collegefreshmanhelper.admin.mapper.AdminOperationLogMapper;
import com.example.collegefreshmanhelper.admin.service.AdminOperationLogService;
import com.example.collegefreshmanhelper.admin.vo.AdminOperationLogVO;
import com.example.collegefreshmanhelper.common.model.PageResult;
import com.example.collegefreshmanhelper.user.entity.SysUser;
import com.example.collegefreshmanhelper.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOperationLogServiceImpl implements AdminOperationLogService {

    private final AdminOperationLogMapper adminOperationLogMapper;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    @Override
    public void record(Long adminUserId, Integer targetType, Long targetId, String operationType,
                       Object beforeData, Object afterData, String reason) {
        AdminOperationLog operationLog = new AdminOperationLog();
        operationLog.setAdminUserId(adminUserId);
        operationLog.setTargetType(targetType);
        operationLog.setTargetId(targetId);
        operationLog.setOperationType(operationType);
        operationLog.setBeforeData(toJson(beforeData));
        operationLog.setAfterData(toJson(afterData));
        operationLog.setReason(reason);
        adminOperationLogMapper.insert(operationLog);
    }

    @Override
    public PageResult<AdminOperationLogVO> pageLogs(long pageNum, long pageSize, Long adminUserId, Integer targetType, String operationType) {
        Page<AdminOperationLog> page = new Page<>(pageNum, pageSize);
        var query = adminOperationLogMapper.selectPage(page, new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AdminOperationLog>()
                .eq(adminUserId != null, AdminOperationLog::getAdminUserId, adminUserId)
                .eq(targetType != null, AdminOperationLog::getTargetType, targetType)
                .eq(StringUtils.hasText(operationType), AdminOperationLog::getOperationType, operationType)
                .orderByDesc(AdminOperationLog::getCreatedAt));

        Map<Long, SysUser> userMap = loadUserMap(query.getRecords().stream().map(AdminOperationLog::getAdminUserId).collect(Collectors.toSet()));
        Page<AdminOperationLogVO> resultPage = new Page<>(query.getCurrent(), query.getSize(), query.getTotal());
        resultPage.setRecords(query.getRecords().stream().map(log -> toVO(log, userMap.get(log.getAdminUserId()))).toList());
        return PageResult.of(resultPage);
    }

    private Map<Long, SysUser> loadUserMap(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
    }

    private AdminOperationLogVO toVO(AdminOperationLog log, SysUser admin) {
        AdminOperationLogVO vo = new AdminOperationLogVO();
        vo.setId(log.getId());
        vo.setAdminUserId(log.getAdminUserId());
        vo.setAdminNickname(admin == null ? "未知管理员" : admin.getNickname());
        vo.setTargetType(log.getTargetType());
        vo.setTargetId(log.getTargetId());
        vo.setOperationType(log.getOperationType());
        vo.setBeforeData(log.getBeforeData());
        vo.setAfterData(log.getAfterData());
        vo.setReason(log.getReason());
        vo.setIp(log.getIp());
        vo.setCreatedAt(log.getCreatedAt());
        return vo;
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return String.valueOf(value);
        }
    }
}
