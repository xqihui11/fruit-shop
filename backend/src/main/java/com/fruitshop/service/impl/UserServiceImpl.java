package com.fruitshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitshop.common.JwtUtil;
import com.fruitshop.common.PasswordUtil;
import com.fruitshop.common.UserContext;
import com.fruitshop.entity.User;
import com.fruitshop.mapper.UserMapper;
import com.fruitshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordUtil passwordUtil;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public User register(String username, String password, String phone) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 暂时不加密，直接存储明文密码
        user.setPhone(phone);
        user.setNickname(username);
        user.setStatus(1);
        // 设置默认头像（使用用户名首字母作为默认头像，前端可以通过Avatar组件显示）
        // 如果后续需要图片文件，可以创建一个默认头像文件
        user.setAvatar(null); // 设置为null，前端Avatar组件会自动显示首字母
        user.setRegisterTime(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }

    @Override
    public String login(String username, String password) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        // 暂时取消密码加密验证，直接比较明文密码（仅用于测试）
        String dbPassword = user.getPassword();
        if (dbPassword == null || !password.trim().equals(dbPassword.trim())) {
            throw new RuntimeException("用户名或密码错误");
        }

        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
        return jwtUtil.generateToken(user.getId(), user.getUsername(), "user");
    }

    @Override
    public User getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return null;
        }
        return userMapper.selectById(userId);
    }

    @Override
    public User getUserByUsername(String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public User updateProfile(User user) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        User existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }
        existingUser.setNickname(user.getNickname());
        existingUser.setPhone(user.getPhone());
        existingUser.setEmail(user.getEmail());
        existingUser.setAvatar(user.getAvatar());
        userMapper.updateById(existingUser);
        return existingUser;
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 暂时取消密码加密验证，直接比较明文密码
        String dbPassword = user.getPassword();
        if (dbPassword == null || !oldPassword.trim().equals(dbPassword.trim())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(newPassword); // 暂时不加密，直接存储明文密码
        userMapper.updateById(user);
    }

    @Override
    public User getSafeUserById(Long id) {
        if (id == null) {
            return null;
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            return null;
        }
        // 创建一个安全的副本，避免返回密码等敏感字段
        User safe = new User();
        safe.setId(user.getId());
        safe.setUsername(user.getUsername());
        safe.setNickname(user.getNickname());
        safe.setAvatar(user.getAvatar());
        safe.setStatus(user.getStatus());
        return safe;
    }
}

