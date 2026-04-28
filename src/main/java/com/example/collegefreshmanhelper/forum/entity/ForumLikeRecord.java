package com.example.collegefreshmanhelper.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.collegefreshmanhelper.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("forum_like_record")
@EqualsAndHashCode(callSuper = true)
public class ForumLikeRecord extends BaseEntity {

    public static final int TARGET_TYPE_POST = 1;
    public static final int TARGET_TYPE_REPLY = 2;
    public static final int STATUS_UNLIKED = 0;
    public static final int STATUS_LIKED = 1;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("target_type")
    private Integer targetType;

    @TableField("target_id")
    private Long targetId;

    @TableField("like_status")
    private Integer likeStatus;

    @TableField("liked_at")
    private LocalDateTime likedAt;

    @TableField("unliked_at")
    private LocalDateTime unlikedAt;
}
