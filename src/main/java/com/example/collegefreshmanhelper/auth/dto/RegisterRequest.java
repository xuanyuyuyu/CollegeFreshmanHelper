package com.example.collegefreshmanhelper.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "请输入账号")
    @Size(min = 4, max = 32, message = "长度需为4到32位")
    private String username;

    @NotBlank(message = "请输入密码")
    @Size(min = 8, max = 32, message = "长度需为8到32位")
    private String password;

    @NotBlank(message = "请输入昵称")
    @Size(max = 32, message = "长度不能超过32位")
    private String nickname;

    @NotNull(message = "请选择身份")
    private Integer role;

    @Min(value = 2000, message = "入学年份需在2000到2100之间")
    @Max(value = 2100, message = "入学年份需在2000到2100之间")
    private Integer admissionYear;
}
