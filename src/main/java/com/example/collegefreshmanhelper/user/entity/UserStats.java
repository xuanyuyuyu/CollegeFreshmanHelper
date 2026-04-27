package com.example.collegefreshmanhelper.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.collegefreshmanhelper.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("user_stats")
@EqualsAndHashCode(callSuper = true)
public class UserStats extends BaseEntity {

    @TableId
    private Long userId;

    private Integer postCount;
    private Integer replyCount;
    private Integer replyLikeReceivedCount;
    private Integer postLikeReceivedCount;
    private Integer knowledgeContributionCount;
    private Integer featuredAnswerCount;
    private Integer rewardAppliedCount;
    private Integer rewardPassedCount;
    private LocalDateTime lastCalculatedAt;
}
