package com.example.collegefreshmanhelper.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminReplyVO {

    private Long id;
    private Long postId;
    private String postTitle;
    private Long userId;
    private String authorNickname;
    private Long parentId;
    private Long replyToReplyId;
    private Long replyToUserId;
    private String replyToUserNickname;
    private String contentPreview;
    private Integer status;
    private Integer visibility;
    private Integer deleted;
    private Integer likeCount;
    private Integer childCount;
    private Integer qaSyncStatus;
    private Long operatorAdminId;
    private String manualDeleteReason;
    private LocalDateTime createdAt;
}
