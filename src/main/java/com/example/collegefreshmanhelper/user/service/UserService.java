package com.example.collegefreshmanhelper.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.collegefreshmanhelper.user.entity.SysUser;

public interface UserService extends IService<SysUser> {

    SysUser getVisibleById(Long id);

    SysUser getActiveUserById(Long id);

    Page<SysUser> pageUsers(long pageNum, long pageSize);

    SysUser findByUsername(String username);
}
