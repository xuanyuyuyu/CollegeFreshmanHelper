package com.example.collegefreshmanhelper.auth.vo;

import com.example.collegefreshmanhelper.user.entity.SysUser;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginVO {

    private String tokenName;
    private String tokenValue;
    private String tokenPrefix;
    private SysUser user;
}
