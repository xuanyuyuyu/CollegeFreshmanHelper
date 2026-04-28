package com.example.collegefreshmanhelper.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminCreateAdminRequest {

    @NotBlank(message = "账号不能为空")
    @Size(min = 4, max = 32, message = "账号长度需在4到32之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在6到32之间")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 32, message = "昵称长度不能超过32")
    private String nickname;
}
