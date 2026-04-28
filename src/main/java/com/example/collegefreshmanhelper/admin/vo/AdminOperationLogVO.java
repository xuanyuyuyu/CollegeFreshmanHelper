package com.example.collegefreshmanhelper.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminOperationLogVO {

    private Long id;
    private Long adminUserId;
    private String adminNickname;
    private Integer targetType;
    private Long targetId;
    private String operationType;
    private String beforeData;
    private String afterData;
    private String reason;
    private String ip;
    private LocalDateTime createdAt;
}
