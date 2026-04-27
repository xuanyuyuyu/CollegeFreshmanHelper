package com.example.collegefreshmanhelper.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminVisibilityUpdateRequest {

    @NotNull
    private Boolean visible;

    private String reason;
}
