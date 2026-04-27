package com.example.collegefreshmanhelper.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminBanUserRequest {

    private String reason;

    private LocalDateTime banExpireAt;
}
