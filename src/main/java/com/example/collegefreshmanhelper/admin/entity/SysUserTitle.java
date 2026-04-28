package com.example.collegefreshmanhelper.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user_title")
public class SysUserTitle {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("title_id")
    private Long titleId;

    @TableField("is_wearing")
    private Integer isWearing;

    @TableField("grant_source")
    private Integer grantSource;

    @TableField("grant_remark")
    private String grantRemark;

    @TableField("granted_by")
    private Long grantedBy;

    @TableField("granted_at")
    private LocalDateTime grantedAt;

    @TableField("expired_at")
    private LocalDateTime expiredAt;
}
