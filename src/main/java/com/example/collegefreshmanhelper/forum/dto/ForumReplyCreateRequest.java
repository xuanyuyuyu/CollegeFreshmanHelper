package com.example.collegefreshmanhelper.forum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForumReplyCreateRequest {

    private Long parentId;

    private Long replyToReplyId;

    private Long replyToUserId;

    @NotBlank
    private String content;

    private String imageUrl;
}
