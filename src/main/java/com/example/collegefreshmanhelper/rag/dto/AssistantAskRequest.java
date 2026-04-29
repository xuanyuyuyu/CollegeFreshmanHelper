package com.example.collegefreshmanhelper.rag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AssistantAskRequest {

    @NotBlank(message = "请输入问题")
    @Size(max = 200, message = "问题长度不能超过 200 个字符")
    private String question;

    @Size(max = 64, message = "分类长度不能超过 64 个字符")
    private String category;

    @Size(max = 64, message = "会话标识长度不能超过 64 个字符")
    private String sessionId;
}
