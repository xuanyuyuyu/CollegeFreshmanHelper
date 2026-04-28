package com.example.collegefreshmanhelper.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminPostVO {

    private Long id;
    private Long userId;
    private String authorNickname;
    private String title;
    private String contentPreview;
    private String tags;
    private Integer status;
    private Integer visibility;
    private Integer deleted;
    private Integer viewCount;
    private Integer replyCount;
    private Integer likeCount;
    private Long operatorAdminId;
    private String manualDeleteReason;
    private LocalDateTime publishedAt;
    private LocalDateTime lastReplyAt;
    private LocalDateTime createdAt;
}
