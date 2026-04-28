package com.example.collegefreshmanhelper.user.vo;

import lombok.Data;

@Data
public class MyCenterLikeVO {

    private Integer postLikeReceivedCount;
    private Integer replyLikeReceivedCount;
    private Integer totalLikeReceivedCount;
    private Integer featuredAnswerCount;
    private Integer knowledgeContributionCount;
    private Integer rewardAppliedCount;
    private Integer rewardPassedCount;
    private Boolean rewardEligible;
    private String rewardHint;
    private String title;
}
