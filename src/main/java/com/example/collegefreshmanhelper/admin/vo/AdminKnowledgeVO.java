package com.example.collegefreshmanhelper.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminKnowledgeVO {

    private Long id;
    private Integer sourceType;
    private Long sourcePostId;
    private Long sourceReplyId;
    private Long contributorUserId;
    private String contributorNickname;
    private Long createdByAdminId;
    private String createdByAdminNickname;
    private String questionText;
    private String answerText;
    private String category;
    private String collectionName;
    private String qdrantPointId;
    private Integer status;
    private Integer likeCountSnapshot;
    private Integer rewardPoints;
    private String failReason;
    private LocalDateTime syncedAt;
    private LocalDateTime createdAt;
}
