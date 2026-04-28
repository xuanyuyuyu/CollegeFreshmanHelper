package com.example.collegefreshmanhelper.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminKnowledgeCreateRequest {

    @NotBlank(message = "问题不能为空")
    @Size(max = 1000, message = "问题长度不能超过1000")
    private String questionText;

    @NotBlank(message = "答案不能为空")
    private String answerText;

    @Size(max = 64, message = "分类长度不能超过64")
    private String category;

    @Size(max = 64, message = "集合名长度不能超过64")
    private String collectionName;

    @Size(max = 64, message = "Embedding模型名长度不能超过64")
    private String embeddingModel;

    @Size(max = 64, message = "LLM模型名长度不能超过64")
    private String llmModel;

    private Integer rewardPoints;
}
