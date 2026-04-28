package com.example.collegefreshmanhelper.common.util;

import cn.dev33.satoken.stp.StpUtil;
import com.example.collegefreshmanhelper.common.exception.BusinessException;

public final class LoginUserContext {

    private LoginUserContext() {
    }

    public static Long getCurrentUserId() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId == null) {
            throw new BusinessException("请先登录");
        }
        try {
            return Long.valueOf(String.valueOf(loginId));
        } catch (NumberFormatException exception) {
            throw new BusinessException("登录态异常");
        }
    }

    public static Long getCurrentUserIdOrNull() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(loginId));
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
