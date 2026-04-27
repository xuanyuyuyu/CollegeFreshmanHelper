package com.example.collegefreshmanhelper.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_operation_log")
public class AdminOperationLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("admin_user_id")
    private Long adminUserId;

    @TableField("target_type")
    private Integer targetType;

    @TableField("target_id")
    private Long targetId;

    @TableField("operation_type")
    private String operationType;

    @TableField("before_data")
    private String beforeData;

    @TableField("after_data")
    private String afterData;

    private String reason;
    private String ip;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
