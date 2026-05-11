package com.fruitshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitshop.entity.User;

public interface UserService extends IService<User> {
    User register(String username, String password, String phone);
    String login(String username, String password);
    User getCurrentUser();
    User getUserByUsername(String username);
    User updateProfile(User user);
    void changePassword(String oldPassword, String newPassword);

    /**
     * 根据ID获取用户的基础信息（只保留展示必要字段，隐藏密码等敏感信息）
     */
    User getSafeUserById(Long id);
}

