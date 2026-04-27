package com.example.collegefreshmanhelper.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.collegefreshmanhelper.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    @JsonIgnore
    @TableField("password_hash")
    private String passwordHash;

    private String nickname;

    @TableField("avatar_url")
    private String avatarUrl;

    private Integer gender;
    private Integer role;

    @TableField("admission_year")
    private Integer admissionYear;

    @TableField("college_name")
    private String collegeName;

    @TableField("major_name")
    private String majorName;

    private String bio;
    private Integer points;
    private Integer status;

    @TableField("ban_expire_at")
    private LocalDateTime banExpireAt;

    @TableField("ban_reason")
    private String banReason;

    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;
}
