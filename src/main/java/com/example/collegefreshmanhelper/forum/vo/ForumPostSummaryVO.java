package com.example.collegefreshmanhelper.forum.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForumPostSummaryVO {

    private Long id;
    private String title;
    private String contentPreview;
    private String tags;
    private Integer replyCount;
    private Integer likeCount;
    private Integer viewCount;
    private Integer imageCount;
    private String firstImageUrl;
    private Boolean liked;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private ForumAuthorVO author;
}
