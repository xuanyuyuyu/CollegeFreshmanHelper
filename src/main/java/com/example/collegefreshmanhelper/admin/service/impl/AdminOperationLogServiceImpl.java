package com.example.collegefreshmanhelper.admin.service.impl;

import com.example.collegefreshmanhelper.admin.entity.AdminOperationLog;
import com.example.collegefreshmanhelper.admin.mapper.AdminOperationLogMapper;
import com.example.collegefreshmanhelper.admin.service.AdminOperationLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminOperationLogServiceImpl implements AdminOperationLogService {

    private final AdminOperationLogMapper adminOperationLogMapper;
    private final ObjectMapper objectMapper;

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
