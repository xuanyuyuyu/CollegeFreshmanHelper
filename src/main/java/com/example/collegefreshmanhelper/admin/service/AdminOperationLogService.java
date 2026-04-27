package com.example.collegefreshmanhelper.admin.service;

public interface AdminOperationLogService {

    void record(Long adminUserId, Integer targetType, Long targetId, String operationType,
                Object beforeData, Object afterData, String reason);
}
