package com.fruitshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitshop.common.JwtUtil;
import com.fruitshop.common.PasswordUtil;
import com.fruitshop.common.UserContext;
import com.fruitshop.entity.*;
import com.fruitshop.mapper.*;
import com.fruitshop.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private PasswordUtil passwordUtil;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String login(String username, String password) {
        QueryWrapper<Admin> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        Admin admin = adminMapper.selectOne(wrapper);
        if (admin == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (admin.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        // 暂时取消密码加密验证，直接比较明文密码（仅用于测试）
        String dbPassword = admin.getPassword();
        if (dbPassword == null || !password.trim().equals(dbPassword.trim())) {
            throw new RuntimeException("用户名或密码错误");
        }

        admin.setLastLoginTime(LocalDateTime.now());
        adminMapper.updateById(admin);
        return jwtUtil.generateToken(admin.getId(), admin.getUsername(), "admin");
    }

    @Override
    public Admin getCurrentAdmin() {
        Long adminId = UserContext.getUserId();
        if (adminId == null) {
            return null;
        }
        return adminMapper.selectById(adminId);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        Long adminId = UserContext.getUserId();
        if (adminId == null) {
            throw new RuntimeException("请先登录");
        }
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        // 暂时取消密码加密验证，直接比较明文密码
        String dbPassword = admin.getPassword();
        if (dbPassword == null || !oldPassword.trim().equals(dbPassword.trim())) {
            throw new RuntimeException("原密码错误");
        }
        admin.setPassword(newPassword); // 暂时不加密，直接存储明文密码
        adminMapper.updateById(admin);
    }

    @Override
    public Map<String, Object> getPlatformStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 用户统计
        long totalUsers = userMapper.selectCount(null);
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("status", 1);
        long activeUsers = userMapper.selectCount(userWrapper);

        // 商家统计
        // 说明：
        // - status = 0：待审核
        // - status = 1：已通过
        // - status = 2：已拒绝（审核不通过）
        // - status = 3：已禁用
        // 按需求：审核不通过的商家（status = 2）不计入商家总数
        QueryWrapper<Merchant> totalMerchantWrapper = new QueryWrapper<>();
        totalMerchantWrapper.ne("status", 2);
        long totalMerchants = merchantMapper.selectCount(totalMerchantWrapper);

        QueryWrapper<Merchant> merchantWrapper = new QueryWrapper<>();
        merchantWrapper.eq("status", 1);
        long activeMerchants = merchantMapper.selectCount(merchantWrapper);
        
        merchantWrapper.clear();
        merchantWrapper.eq("status", 0);
        long pendingMerchants = merchantMapper.selectCount(merchantWrapper);

        // 商品统计
        long totalProducts = productMapper.selectCount(null);
        QueryWrapper<Product> productWrapper = new QueryWrapper<>();
        productWrapper.eq("status", 0);
        long pendingProducts = productMapper.selectCount(productWrapper);

        // 订单统计
        long totalOrders = orderMapper.selectCount(null);
        
        // 销售额统计
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.eq("payment_status", 1);
        orderWrapper.select("SUM(pay_amount) as payAmount");
        List<Order> orders = orderMapper.selectList(orderWrapper);
        BigDecimal totalSales = BigDecimal.ZERO;
        if (orders != null && !orders.isEmpty() && orders.get(0) != null && orders.get(0).getPayAmount() != null) {
            totalSales = orders.get(0).getPayAmount();
        }

        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("totalMerchants", totalMerchants);
        stats.put("activeMerchants", activeMerchants);
        stats.put("pendingMerchants", pendingMerchants);
        stats.put("totalProducts", totalProducts);
        stats.put("pendingProducts", pendingProducts);
        stats.put("totalOrders", totalOrders);
        stats.put("totalSales", totalSales);

        return stats;
    }
}

