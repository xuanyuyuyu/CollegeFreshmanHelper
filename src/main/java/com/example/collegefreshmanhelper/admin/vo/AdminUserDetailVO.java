package com.example.collegefreshmanhelper.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserDetailVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private Integer role;
    private Integer status;
    private Integer gender;
    private Integer admissionYear;
    private String collegeName;
    private String majorName;
    private String bio;
    private Integer points;
    private Integer postCount;
    private Integer replyCount;
    private Integer postLikeReceivedCount;
    private Integer replyLikeReceivedCount;
    private Integer knowledgeContributionCount;
    private Integer featuredAnswerCount;
    private String banReason;
    private LocalDateTime banExpireAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
