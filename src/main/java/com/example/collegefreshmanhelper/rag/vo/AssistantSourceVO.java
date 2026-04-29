package com.example.collegefreshmanhelper.rag.vo;

import lombok.Data;

@Data
public class AssistantSourceVO {

    private Long knowledgeId;

    private String questionText;

    private String answerText;

    private String category;

    private Long sourcePostId;

    private Long sourceReplyId;

    private Long contributorUserId;

    private String sourceLabel;
}
