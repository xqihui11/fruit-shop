package com.fruitshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitshop.common.JwtUtil;
import com.fruitshop.common.PasswordUtil;
import com.fruitshop.common.UserContext;
import com.fruitshop.entity.AfterSale;
import com.fruitshop.entity.Merchant;
import com.fruitshop.entity.Order;
import com.fruitshop.entity.Product;
import com.fruitshop.mapper.AfterSaleMapper;
import com.fruitshop.mapper.MerchantMapper;
import com.fruitshop.mapper.OrderMapper;
import com.fruitshop.mapper.ProductMapper;
import com.fruitshop.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements MerchantService {
    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private PasswordUtil passwordUtil;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AfterSaleMapper afterSaleMapper;

    @Override
    public Merchant register(Merchant merchant) {
        QueryWrapper<Merchant> wrapper = new QueryWrapper<>();
        wrapper.eq("username", merchant.getUsername());
        if (merchantMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        merchant.setPassword(merchant.getPassword()); // 暂时不加密，直接存储明文密码
        merchant.setStatus(0); // 待审核
        merchant.setRegisterTime(LocalDateTime.now());
        merchantMapper.insert(merchant);
        return merchant;
    }

    @Override
    public String login(String username, String password) {
        QueryWrapper<Merchant> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        Merchant merchant = merchantMapper.selectOne(wrapper);
        if (merchant == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (merchant.getStatus() == 0) {
            throw new RuntimeException("账号正在审核中");
        }
        if (merchant.getStatus() == 2) {
            throw new RuntimeException("账号审核未通过");
        }
        if (merchant.getStatus() == 3) {
            throw new RuntimeException("账号已被禁用");
        }
        // 暂时取消密码加密验证，直接比较明文密码（仅用于测试）
        String dbPassword = merchant.getPassword();
        if (dbPassword == null || !password.trim().equals(dbPassword.trim())) {
            throw new RuntimeException("用户名或密码错误");
        }

        return jwtUtil.generateToken(merchant.getId(), merchant.getUsername(), "merchant");
    }

    @Override
    public Merchant getCurrentMerchant() {
        Long merchantId = UserContext.getUserId();
        if (merchantId == null) {
            return null;
        }
        return merchantMapper.selectById(merchantId);
    }

    @Override
    public Merchant updateMerchant(Merchant merchant) {
        Long merchantId = UserContext.getUserId();
        if (merchantId == null) {
            throw new RuntimeException("请先登录");
        }
        Merchant existing = merchantMapper.selectById(merchantId);
        if (existing == null) {
            throw new RuntimeException("商家不存在");
        }
        existing.setShopName(merchant.getShopName());
        existing.setShopDescription(merchant.getShopDescription());
        // 新增：店铺地址、营业时间与营业状态
        existing.setShopAddress(merchant.getShopAddress());
        existing.setBusinessHours(merchant.getBusinessHours());
        existing.setBusinessStatus(merchant.getBusinessStatus());
        existing.setContactName(merchant.getContactName());
        existing.setContactPhone(merchant.getContactPhone());
        existing.setContactEmail(merchant.getContactEmail());
        // 更新头像（如果提供了）
        if (merchant.getAvatar() != null) {
            existing.setAvatar(merchant.getAvatar());
        }
        merchantMapper.updateById(existing);
        return existing;
    }

    @Override
    public List<Merchant> getPendingMerchants() {
        QueryWrapper<Merchant> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        wrapper.orderByDesc("register_time");
        return merchantMapper.selectList(wrapper);
    }

    @Override
    public void auditMerchant(Long id, boolean approve, Long adminId) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new RuntimeException("商家不存在");
        }
        merchant.setStatus(approve ? 1 : 2);
        merchant.setAuditTime(LocalDateTime.now());
        merchant.setAuditAdminId(adminId);
        merchantMapper.updateById(merchant);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Long merchantId = UserContext.getUserId();
        Map<String, Object> stats = new HashMap<>();

        // 商品统计
        QueryWrapper<Product> productWrapper = new QueryWrapper<>();
        productWrapper.eq("merchant_id", merchantId);
        long totalProducts = productMapper.selectCount(productWrapper);
        
        productWrapper.clear();
        productWrapper.eq("merchant_id", merchantId);
        productWrapper.eq("status", 1);
        long onSaleProducts = productMapper.selectCount(productWrapper);

        // 订单统计
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.eq("merchant_id", merchantId);
        long totalOrders = orderMapper.selectCount(orderWrapper);

        // 今日订单数
        orderWrapper.clear();
        orderWrapper.eq("merchant_id", merchantId);
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        orderWrapper.ge("create_time", todayStart);
        long todayOrders = orderMapper.selectCount(orderWrapper);

        orderWrapper.clear();
        orderWrapper.eq("merchant_id", merchantId);
        orderWrapper.eq("order_status", 1);
        long pendingShipOrders = orderMapper.selectCount(orderWrapper);

        // 销售额统计
        orderWrapper.clear();
        orderWrapper.eq("merchant_id", merchantId);
        orderWrapper.eq("payment_status", 1);
        orderWrapper.select("SUM(pay_amount) as payAmount");
        List<Order> orders = orderMapper.selectList(orderWrapper);
        BigDecimal totalSales = BigDecimal.ZERO;
        if (orders != null && !orders.isEmpty() && orders.get(0).getPayAmount() != null) {
            totalSales = orders.get(0).getPayAmount();
        }

        // 近7日订单与销售额统计
        orderWrapper.clear();
        orderWrapper.eq("merchant_id", merchantId);
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();
        orderWrapper.ge("create_time", sevenDaysAgo);
        List<Order> last7Orders = orderMapper.selectList(orderWrapper);
        long last7DaysOrders = 0L;
        BigDecimal last7DaysSales = BigDecimal.ZERO;
        if (last7Orders != null) {
            for (Order o : last7Orders) {
                last7DaysOrders++;
                if (o.getPaymentStatus() != null && o.getPaymentStatus() == 1 && o.getPayAmount() != null) {
                    last7DaysSales = last7DaysSales.add(o.getPayAmount());
                }
            }
        }

        // 售后统计
        QueryWrapper<AfterSale> afterWrapper = new QueryWrapper<>();
        afterWrapper.eq("merchant_id", merchantId);
        long totalAfterSales = afterSaleMapper.selectCount(afterWrapper);

        afterWrapper.clear();
        afterWrapper.eq("merchant_id", merchantId);
        afterWrapper.eq("status", 0);
        long pendingAfterSales = afterSaleMapper.selectCount(afterWrapper);

        afterWrapper.clear();
        afterWrapper.eq("merchant_id", merchantId);
        afterWrapper.in("status", Arrays.asList(1, 3));
        long completedAfterSales = afterSaleMapper.selectCount(afterWrapper);

        // 客单价与退款率
        BigDecimal avgOrderValue = BigDecimal.ZERO;
        if (totalOrders > 0 && totalSales.compareTo(BigDecimal.ZERO) > 0) {
            avgOrderValue = totalSales.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP);
        }
        double refundRate = 0.0;
        if (totalOrders > 0 && completedAfterSales > 0) {
            refundRate = (double) completedAfterSales / (double) totalOrders;
        }

        stats.put("totalProducts", totalProducts);
        stats.put("onSaleProducts", onSaleProducts);
        stats.put("totalOrders", totalOrders);
        stats.put("pendingShipOrders", pendingShipOrders);
        stats.put("totalSales", totalSales);
        stats.put("todayOrders", todayOrders);
        stats.put("last7DaysOrders", last7DaysOrders);
        stats.put("last7DaysSales", last7DaysSales);
        stats.put("totalAfterSales", totalAfterSales);
        stats.put("pendingAfterSales", pendingAfterSales);
        stats.put("avgOrderValue", avgOrderValue);
        stats.put("refundRate", refundRate);

        return stats;
    }

    @Override
    public List<Merchant> getActiveMerchants() {
        QueryWrapper<Merchant> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1); // 只返回已通过审核的店铺
        wrapper.orderByDesc("register_time");
        return list(wrapper);
    }

    @Override
    public Merchant getMerchantDetail(Long id) {
        Merchant merchant = getById(id);
        if (merchant == null || merchant.getStatus() != 1) {
            throw new RuntimeException("店铺不存在或未通过审核");
        }
        return merchant;
    }
}

