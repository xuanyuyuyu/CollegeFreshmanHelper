package com.example.collegefreshmanhelper.admin.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminRevokeTitleRequest {

    @Size(max = 255, message = "撤销原因长度不能超过255")
    private String reason;
}
