package com.example.collegefreshmanhelper.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserSummaryVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private Integer role;
    private Integer status;
    private Integer points;
    private Integer postCount;
    private Integer replyCount;
    private Integer totalLikeReceivedCount;
    private Integer knowledgeContributionCount;
    private Integer featuredAnswerCount;
    private LocalDateTime banExpireAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
