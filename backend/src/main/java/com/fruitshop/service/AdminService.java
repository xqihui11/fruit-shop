package com.fruitshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitshop.entity.Admin;

import java.util.Map;

public interface AdminService extends IService<Admin> {
    String login(String username, String password);
    Admin getCurrentAdmin();
    void changePassword(String oldPassword, String newPassword);
    Map<String, Object> getPlatformStatistics();
}

