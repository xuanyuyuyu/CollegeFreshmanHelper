package com.example.collegefreshmanhelper.user.vo;

import lombok.Data;

@Data
public class UserTitleRuleVO {

    private Long id;
    private String titleName;
    private String titleCode;
    private String description;
    private Integer ruleType;
    private Integer ruleThreshold;
    private Integer sortNo;
}
