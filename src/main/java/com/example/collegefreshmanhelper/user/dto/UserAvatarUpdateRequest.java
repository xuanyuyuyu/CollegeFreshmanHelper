package com.example.collegefreshmanhelper.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserAvatarUpdateRequest {

    @NotBlank(message = "头像地址不能为空")
    @Size(max = 500, message = "头像地址过长")
    private String avatarUrl;
}
