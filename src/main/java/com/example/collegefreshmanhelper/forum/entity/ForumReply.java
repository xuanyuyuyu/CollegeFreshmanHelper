package com.example.collegefreshmanhelper.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.collegefreshmanhelper.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("forum_reply")
@EqualsAndHashCode(callSuper = true)
public class ForumReply extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("post_id")
    private Long postId;

    @TableField("user_id")
    private Long userId;

    @TableField("parent_id")
    private Long parentId;

    @TableField("reply_to_reply_id")
    private Long replyToReplyId;

    @TableField("reply_to_user_id")
    private Long replyToUserId;

    private String content;

    @TableField("content_type")
    private Integer contentType;

    @TableField("image_url")
    private String imageUrl;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("child_count")
    private Integer childCount;

    private Integer status;
    private Integer visibility;

    @TableField("qa_sync_status")
    private Integer qaSyncStatus;

    @TableField("operator_admin_id")
    private Long operatorAdminId;

    @TableField("manual_delete_reason")
    private String manualDeleteReason;
}
