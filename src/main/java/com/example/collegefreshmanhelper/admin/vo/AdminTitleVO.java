package com.example.collegefreshmanhelper.admin.vo;

import lombok.Data;

@Data
public class AdminTitleVO {

    private Long id;
    private String titleName;
    private String titleCode;
    private String description;
    private Integer grantType;
    private Integer ruleType;
    private Integer ruleThreshold;
    private Integer status;
    private Integer sortNo;
}
