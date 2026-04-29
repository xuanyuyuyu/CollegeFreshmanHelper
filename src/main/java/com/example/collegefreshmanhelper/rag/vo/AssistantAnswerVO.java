package com.example.collegefreshmanhelper.rag.vo;

import lombok.Data;

import java.util.List;

@Data
public class AssistantAnswerVO {

    private String question;

    private String answer;

    private Integer matchedCount;

    private Boolean fallback;

    private List<AssistantSourceVO> sources;
}
