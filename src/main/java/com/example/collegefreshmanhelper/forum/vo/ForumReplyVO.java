package com.example.collegefreshmanhelper.forum.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForumReplyVO {

    private Long id;
    private Long postId;
    private Long userId;
    private String userNickname;
    private String userAvatarUrl;
    private String userTitle;
    private Boolean postAuthor;
    private Long parentId;
    private Long replyToReplyId;
    private Long replyToUserId;
    private String replyToUserNickname;
    private String replyToUserTitle;
    private String content;
    private Integer contentType;
    private String imageUrl;
    private Integer likeCount;
    private Boolean liked;
    private Integer childCount;
    private LocalDateTime createdAt;
}
