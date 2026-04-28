package com.example.collegefreshmanhelper.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminKnowledgeStatusUpdateRequest {

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String reason;
}
