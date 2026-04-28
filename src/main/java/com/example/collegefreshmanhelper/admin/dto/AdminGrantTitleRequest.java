package com.example.collegefreshmanhelper.admin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminGrantTitleRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "头衔ID不能为空")
    private Long titleId;

    private Boolean wearing;

    @Size(max = 255, message = "发放备注长度不能超过255")
    private String remark;

    private LocalDateTime expiredAt;
}
