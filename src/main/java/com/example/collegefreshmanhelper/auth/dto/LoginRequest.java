package com.example.collegefreshmanhelper.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "请输入账号")
    private String username;

    @NotBlank(message = "请输入密码")
    @Size(min = 8, max = 32, message = "长度需为8到32位")
    private String password;
}
