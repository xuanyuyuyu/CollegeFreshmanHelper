package com.example.collegefreshmanhelper.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MyCenterPostVO {

    private Long id;
    private String title;
    private String contentPreview;
    private String tags;
    private Integer status;
    private Integer visibility;
    private Integer replyCount;
    private Integer likeCount;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}
