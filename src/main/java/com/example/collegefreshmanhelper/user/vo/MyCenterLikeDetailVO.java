package com.example.collegefreshmanhelper.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MyCenterLikeDetailVO {

    private String targetType;
    private Long targetId;
    private Long postId;
    private String postTitle;
    private String targetTitle;
    private String contentPreview;
    private Integer likeCount;
    private Integer status;
    private Integer visibility;
    private LocalDateTime createdAt;
}
