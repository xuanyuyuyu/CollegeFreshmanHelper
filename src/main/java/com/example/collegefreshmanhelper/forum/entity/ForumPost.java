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
@TableName("forum_post")
@EqualsAndHashCode(callSuper = true)
public class ForumPost extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("user_id")
    private Long userId;

    private String title;
    private String content;

    @TableField("content_type")
    private Integer contentType;

    @TableField("first_image_url")
    private String firstImageUrl;

    @TableField("image_count")
    private Integer imageCount;

    private String tags;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("reply_count")
    private Integer replyCount;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("collect_count")
    private Integer collectCount;

    private Integer status;
    private Integer visibility;

    @TableField("audit_reason")
    private String auditReason;

    @TableField("operator_admin_id")
    private Long operatorAdminId;

    @TableField("manual_delete_reason")
    private String manualDeleteReason;

    @TableField("published_at")
    private LocalDateTime publishedAt;

    @TableField("last_reply_at")
    private LocalDateTime lastReplyAt;
}
