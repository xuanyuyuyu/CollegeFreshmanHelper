package com.example.collegefreshmanhelper.rag.vo;

import lombok.Data;

import java.util.List;

@Data
public class AssistantStreamChunkVO {

    private String type;

    private String question;

    private String content;

    private String answer;

    private Integer matchedCount;

    private Boolean fallback;

    private String message;

    private List<AssistantSourceVO> sources;
}
