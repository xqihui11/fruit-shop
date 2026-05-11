package com.fruitshop.controller;

import com.fruitshop.common.Result;
import com.fruitshop.entity.User;
import com.fruitshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<User> register(@RequestBody Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");
            String phone = params.get("phone");
            User user = userService.register(username, password, phone);
            if (user != null) {
                user.setPassword(null); // 不返回密码
            }
            return Result.success("注册成功", user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");
            String token = userService.login(username, password);
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            // 登录后根据用户名查询用户信息，而不是使用getCurrentUser（此时UserContext可能还没有设置）
            User user = userService.getUserByUsername(username);
            if (user != null) {
                user.setPassword(null);
            }
            data.put("user", user);
            return Result.success("登录成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<User> getCurrentUser() {
        try {
            User user = userService.getCurrentUser();
            if (user == null) {
                return Result.error(401, "未登录");
            }
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody User user) {
        try {
            User updatedUser = userService.updateProfile(user);
            updatedUser.setPassword(null);
            return Result.success("保存成功", updatedUser);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/changePassword")
    public Result<Void> changePassword(@RequestBody Map<String, String> params) {
        try {
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");
            userService.changePassword(oldPassword, newPassword);
            return Result.success("密码修改成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 商家端：根据用户ID列表批量获取用户基础信息（含头像、昵称），用于聊天展示
     */
    @PostMapping("/basic-info/batch")
    public Result<List<User>> getBasicUserInfoBatch(@RequestBody Map<String, List<Long>> body) {
        try {
            List<Long> ids = body.get("userIds");
            if (ids == null || ids.isEmpty()) {
                return Result.success(List.of());
            }
            List<User> list = ids.stream()
                    .distinct()
                    .map(userService::getSafeUserById)
                    .filter(u -> u != null)
                    .collect(Collectors.toList());
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

