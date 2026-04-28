package com.example.collegefreshmanhelper.admin.service;

import com.example.collegefreshmanhelper.admin.vo.AdminOperationLogVO;
import com.example.collegefreshmanhelper.common.model.PageResult;

public interface AdminOperationLogService {

    void record(Long adminUserId, Integer targetType, Long targetId, String operationType,
                Object beforeData, Object afterData, String reason);

    PageResult<AdminOperationLogVO> pageLogs(long pageNum, long pageSize, Long adminUserId, Integer targetType, String operationType);
}
