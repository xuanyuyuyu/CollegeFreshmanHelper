package com.example.collegefreshmanhelper.auth.config;

import cn.dev33.satoken.stp.StpInterface;
import com.example.collegefreshmanhelper.user.entity.SysUser;
import com.example.collegefreshmanhelper.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserService userService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        SysUser user = userService.getById(Long.valueOf(String.valueOf(loginId)));
        if (user == null) {
            return Collections.emptyList();
        }

        List<String> roles = new ArrayList<>();
        roles.add("user");
        if (Integer.valueOf(1).equals(user.getRole())) {
            roles.add("freshman");
        }
        if (Integer.valueOf(2).equals(user.getRole())) {
            roles.add("senior");
        }
        if (Integer.valueOf(8).equals(user.getRole())) {
            roles.add("admin");
        }
        if (Integer.valueOf(9).equals(user.getRole())) {
            roles.add("admin");
            roles.add("super-admin");
        }
        return roles;
    }
}
