package com.example.collegefreshmanhelper.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 32, message = "昵称长度不能超过32位")
    private String nickname;

    @Min(value = 0, message = "性别参数非法")
    @Max(value = 2, message = "性别参数非法")
    private Integer gender;

    @Min(value = 2000, message = "入学年份不合法")
    @Max(value = 2100, message = "入学年份不合法")
    private Integer admissionYear;

    @Size(max = 64, message = "学院名称过长")
    private String collegeName;

    @Size(max = 64, message = "专业名称过长")
    private String majorName;

    @Size(max = 255, message = "个人简介过长")
    private String bio;
}
