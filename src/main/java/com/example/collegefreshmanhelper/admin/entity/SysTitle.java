package com.example.collegefreshmanhelper.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_title")
public class SysTitle {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("title_name")
    private String titleName;

    @TableField("title_code")
    private String titleCode;

    private String description;

    @TableField("icon_url")
    private String iconUrl;

    @TableField("grant_type")
    private Integer grantType;

    @TableField("rule_type")
    private Integer ruleType;

    @TableField("rule_threshold")
    private Integer ruleThreshold;

    private Integer status;

    @TableField("sort_no")
    private Integer sortNo;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
