package com.example.collegefreshmanhelper.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminGrantTitleRequest {

    @NotBlank(message = "账号不能为空")
    @Size(max = 32, message = "账号长度不能超过32位")
    private String username;

    @NotNull(message = "头衔ID不能为空")
    private Long titleId;

    private Boolean wearing;

    @Size(max = 255, message = "发放备注长度不能超过255")
    private String remark;

    private LocalDateTime expiredAt;
}
