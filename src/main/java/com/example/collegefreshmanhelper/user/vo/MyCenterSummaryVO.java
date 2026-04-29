package com.example.collegefreshmanhelper.user.vo;

import lombok.Data;

import java.util.List;

@Data
public class MyCenterSummaryVO {

    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private Integer role;
    private Integer gender;
    private Integer admissionYear;
    private String collegeName;
    private String majorName;
    private String bio;
    private Integer points;
    private Integer status;
    private Integer postCount;
    private Integer replyCount;
    private Integer totalLikeReceivedCount;
    private Integer knowledgeContributionCount;
    private Integer featuredAnswerCount;
    private String title;
    private List<UserTitleRuleVO> titleRules;
}
