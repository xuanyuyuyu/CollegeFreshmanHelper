package com.example.collegefreshmanhelper.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserTitleVO {

    private Long id;
    private Long userId;
    private String userNickname;
    private Long titleId;
    private String titleName;
    private Integer isWearing;
    private Integer grantSource;
    private String grantRemark;
    private Long grantedBy;
    private String grantedByNickname;
    private LocalDateTime grantedAt;
    private LocalDateTime expiredAt;
}
