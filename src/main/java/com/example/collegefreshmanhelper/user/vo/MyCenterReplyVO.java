package com.example.collegefreshmanhelper.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MyCenterReplyVO {

    private Long id;
    private Long postId;
    private String postTitle;
    private Long parentId;
    private Long replyToReplyId;
    private Long replyToUserId;
    private String replyToUserNickname;
    private String contentPreview;
    private Integer likeCount;
    private Integer childCount;
    private Integer status;
    private Integer visibility;
    private LocalDateTime createdAt;
}
