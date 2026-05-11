package com.fruitshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitshop.common.UserContext;
import com.fruitshop.entity.Coupon;
import com.fruitshop.entity.UserCoupon;
import com.fruitshop.mapper.CouponMapper;
import com.fruitshop.mapper.UserCouponMapper;
import com.fruitshop.mapper.MerchantMapper;
import com.fruitshop.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {
    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private UserCouponMapper userCouponMapper;
    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    public List<Coupon> getAvailableCoupons() {
        return getAvailableCoupons(null);
    }
    
    @Override
    public List<Coupon> getAvailableCoupons(Long merchantId) {
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<Coupon> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1); // 进行中
        wrapper.le("valid_start_time", now);
        wrapper.ge("valid_end_time", now);
        wrapper.and(w -> w.eq("total_count", 0).or().apply("received_count < total_count")); // 未领完或无限量
        
        // 如果指定了商家ID，只返回平台优惠券（merchantId为null）和该商家的优惠券
        if (merchantId != null) {
            wrapper.and(w -> w.isNull("merchant_id").or().eq("merchant_id", merchantId));
        }
        // 如果没有指定商家ID，返回所有优惠券（平台+所有商家）
        
        wrapper.orderByDesc("create_time");
        
        List<Coupon> coupons = list(wrapper);
        
        // 检查用户是否已领取
        Long userId = UserContext.getUserId();
        if (userId != null) {
            for (Coupon coupon : coupons) {
                QueryWrapper<UserCoupon> userCouponWrapper = new QueryWrapper<>();
                userCouponWrapper.eq("user_id", userId);
                userCouponWrapper.eq("coupon_id", coupon.getId());
                userCouponWrapper.in("status", 0, 1); // 未使用或已使用
                long count = userCouponMapper.selectCount(userCouponWrapper);
                coupon.setCanReceive(count == 0); // 未领取过才能领取
            }
        } else {
            coupons.forEach(c -> c.setCanReceive(true));
        }
        
        // 填充商家名称（如果有merchantId）
        for (Coupon coupon : coupons) {
            if (coupon.getMerchantId() != null) {
                try {
                    com.fruitshop.entity.Merchant merchant = merchantMapper.selectById(coupon.getMerchantId());
                    if (merchant != null) {
                        coupon.setMerchantName(merchant.getShopName());
                    }
                } catch (Exception e) {
                    // 忽略错误，商家名称保持为null
                }
            }
        }
        
        return coupons;
    }

    @Override
    @Transactional
    public void receiveCoupon(Long couponId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStatus() != 1) {
            throw new RuntimeException("优惠券已下架");
        }
        if (now.isBefore(coupon.getValidStartTime()) || now.isAfter(coupon.getValidEndTime())) {
            throw new RuntimeException("优惠券不在有效期内");
        }
        if (coupon.getTotalCount() > 0 && coupon.getReceivedCount() >= coupon.getTotalCount()) {
            throw new RuntimeException("优惠券已领完");
        }

        // 检查是否已领取
        QueryWrapper<UserCoupon> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("coupon_id", couponId);
        wrapper.in("status", 0, 1); // 未使用或已使用
        if (userCouponMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("您已领取过该优惠券");
        }

        // 创建用户优惠券记录
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(0); // 未使用
        userCoupon.setExpireTime(coupon.getValidEndTime());
        userCouponMapper.insert(userCoupon);

        // 更新优惠券已领取数量
        coupon.setReceivedCount(coupon.getReceivedCount() + 1);
        couponMapper.updateById(coupon);
    }

    @Override
    public List<Coupon> getUserCoupons(Integer status) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        QueryWrapper<UserCoupon> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("receive_time");
        List<UserCoupon> userCoupons = userCouponMapper.selectList(wrapper);

        // 检查过期状态
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekAgo = now.minusWeeks(1); // 1周前的时间
        
        for (UserCoupon userCoupon : userCoupons) {
            if (userCoupon.getStatus() == 0 && now.isAfter(userCoupon.getExpireTime())) {
                userCoupon.setStatus(2); // 已过期
                userCouponMapper.updateById(userCoupon);
            }
        }

        // 重新查询以获取更新后的状态
        if (status == null || status == 0) {
            wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            if (status != null) {
                wrapper.eq("status", status);
            }
            wrapper.orderByDesc("receive_time");
            userCoupons = userCouponMapper.selectList(wrapper);
        }

        // 过滤掉超过1周的已使用或已过期优惠券
        userCoupons = userCoupons.stream()
                .filter(uc -> {
                    // 未使用的优惠券始终显示
                    if (uc.getStatus() == 0) {
                        return true;
                    }
                    // 已使用或已过期的优惠券，检查时间
                    LocalDateTime checkTime = null;
                    if (uc.getStatus() == 1 && uc.getUsedTime() != null) {
                        // 已使用：检查使用时间
                        checkTime = uc.getUsedTime();
                    } else if (uc.getStatus() == 2 && uc.getExpireTime() != null) {
                        // 已过期：检查过期时间
                        checkTime = uc.getExpireTime();
                    }
                    // 如果没有时间信息，保留（兼容旧数据）
                    if (checkTime == null) {
                        return true;
                    }
                    // 只保留1周内的记录
                    return checkTime.isAfter(oneWeekAgo);
                })
                .collect(java.util.stream.Collectors.toList());

        // 关联优惠券信息
        for (UserCoupon userCoupon : userCoupons) {
            Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
            userCoupon.setCoupon(coupon);
        }

        // 转换为Coupon列表（包含使用状态）
        List<Coupon> result = userCoupons.stream()
                .map(uc -> {
                    Coupon c = uc.getCoupon();
                    if (c != null) {
                        c.setCanReceive(uc.getStatus() == 0); // 未使用
                    }
                    return c;
                })
                .filter(c -> c != null)
                .collect(java.util.stream.Collectors.toList());
        
        // 填充商家名称（如果有merchantId）
        for (Coupon coupon : result) {
            if (coupon.getMerchantId() != null) {
                try {
                    com.fruitshop.entity.Merchant merchant = merchantMapper.selectById(coupon.getMerchantId());
                    if (merchant != null) {
                        coupon.setMerchantName(merchant.getShopName());
                    }
                } catch (Exception e) {
                    // 忽略错误，商家名称保持为null
                    e.printStackTrace();
                }
            }
        }
        
        return result;
    }

    @Override
    @Transactional
    public void clearExpiredCoupons(Integer status) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        
        if (status == null || (status != 1 && status != 2)) {
            throw new RuntimeException("状态参数无效，只能是1（已使用）或2（已过期）");
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekAgo = now.minusWeeks(1); // 1周前的时间
        
        QueryWrapper<UserCoupon> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("status", status);
        
        if (status == 1) {
            // 已使用：清除使用时间超过1周的
            wrapper.le("used_time", oneWeekAgo);
        } else if (status == 2) {
            // 已过期：清除过期时间超过1周的
            wrapper.le("expire_time", oneWeekAgo);
        }
        
        userCouponMapper.delete(wrapper);
    }

    @Override
    public Coupon createCoupon(Coupon coupon) {
        // 验证必填字段
        if (coupon.getName() == null || coupon.getName().trim().isEmpty()) {
            throw new RuntimeException("优惠券名称不能为空");
        }
        if (coupon.getType() == null) {
            throw new RuntimeException("优惠券类型不能为空");
        }
        if (coupon.getValidStartTime() == null) {
            throw new RuntimeException("有效期开始时间不能为空");
        }
        if (coupon.getValidEndTime() == null) {
            throw new RuntimeException("有效期结束时间不能为空");
        }
        if (coupon.getValidStartTime().isAfter(coupon.getValidEndTime())) {
            throw new RuntimeException("有效期开始时间不能晚于结束时间");
        }
        
        // 根据优惠券类型验证必填字段
        if (coupon.getType() == 1) {
            // 满减券需要优惠金额
            if (coupon.getDiscountAmount() == null || coupon.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("满减券的优惠金额必须大于0");
            }
        } else if (coupon.getType() == 2) {
            // 折扣券需要折扣率
            if (coupon.getDiscountRate() == null || coupon.getDiscountRate().compareTo(java.math.BigDecimal.ZERO) <= 0 || coupon.getDiscountRate().compareTo(java.math.BigDecimal.ONE) > 0) {
                throw new RuntimeException("折扣券的折扣率必须在0到1之间");
            }
        } else {
            throw new RuntimeException("优惠券类型无效，只能是1（满减券）或2（折扣券）");
        }
        
        // 验证最低使用金额
        if (coupon.getMinAmount() == null || coupon.getMinAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new RuntimeException("最低使用金额不能小于0");
        }
        
        // 根据用户角色设置merchantId
        String role = UserContext.getRole();
        if ("merchant".equals(role)) {
            // 商家创建的优惠券，自动设置merchantId
            Long merchantId = UserContext.getUserId();
            if (merchantId == null) {
                throw new RuntimeException("无法获取商家信息");
            }
            coupon.setMerchantId(merchantId);
        } else if ("admin".equals(role)) {
            // 管理员创建的优惠券，merchantId为null（平台优惠券，全平台可用）
            coupon.setMerchantId(null);
        } else {
            // 其他角色不允许创建优惠券
            throw new RuntimeException("无权限创建优惠券");
        }
        
        // 设置默认值
        if (coupon.getReceivedCount() == null) {
            coupon.setReceivedCount(0);
        }
        if (coupon.getUsedCount() == null) {
            coupon.setUsedCount(0);
        }
        if (coupon.getStatus() == null) {
            coupon.setStatus(1); // 默认进行中
        }
        if (coupon.getTotalCount() == null) {
            coupon.setTotalCount(0); // 0表示不限制
        }
        
        LocalDateTime now = LocalDateTime.now();
        // 根据时间设置状态
        if (now.isAfter(coupon.getValidEndTime())) {
            coupon.setStatus(2); // 已结束
        } else if (now.isBefore(coupon.getValidStartTime())) {
            coupon.setStatus(1); // 进行中
        }
        
        couponMapper.insert(coupon);
        return coupon;
    }

    @Override
    public Coupon updateCoupon(Coupon coupon) {
        Coupon existing = couponMapper.selectById(coupon.getId());
        if (existing == null) {
            throw new RuntimeException("优惠券不存在");
        }
        
        // 验证权限：商家只能更新自己的优惠券
        String role = UserContext.getRole();
        if ("merchant".equals(role)) {
            Long merchantId = UserContext.getUserId();
            if (existing.getMerchantId() == null || !existing.getMerchantId().equals(merchantId)) {
                throw new RuntimeException("无权限修改此优惠券");
            }
            // 确保商家不能修改merchantId
            coupon.setMerchantId(existing.getMerchantId());
        } else if ("admin".equals(role)) {
            // 管理员可以修改所有优惠券，包括merchantId
        } else {
            throw new RuntimeException("无权限修改优惠券");
        }
        
        if (coupon.getValidStartTime() != null && coupon.getValidEndTime() != null) {
            if (coupon.getValidStartTime().isAfter(coupon.getValidEndTime())) {
                throw new RuntimeException("有效期开始时间不能晚于结束时间");
            }
        }
        
        couponMapper.updateById(coupon);
        return coupon;
    }

    @Override
    @Transactional
    public void deleteCoupon(Long id) {
        Coupon existing = couponMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("优惠券不存在");
        }
        
        // 验证权限：商家只能删除自己的优惠券
        String role = UserContext.getRole();
        if ("merchant".equals(role)) {
            Long merchantId = UserContext.getUserId();
            if (existing.getMerchantId() == null || !existing.getMerchantId().equals(merchantId)) {
                throw new RuntimeException("无权限删除此优惠券");
            }
        } else if (!"admin".equals(role)) {
            throw new RuntimeException("无权限删除优惠券");
        }
        
        // 检查是否有用户已领取
        QueryWrapper<UserCoupon> wrapper = new QueryWrapper<>();
        wrapper.eq("coupon_id", id);
        wrapper.in("status", 0); // 未使用
        if (userCouponMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该优惠券已被用户领取，无法删除");
        }
        couponMapper.deleteById(id);
    }

    @Override
    public List<Coupon> getAllCoupons() {
        QueryWrapper<Coupon> wrapper = new QueryWrapper<>();
        
        // 根据用户角色过滤
        String role = UserContext.getRole();
        if ("merchant".equals(role)) {
            // 商家只能查看自己的优惠券
            Long merchantId = UserContext.getUserId();
            if (merchantId != null) {
                wrapper.eq("merchant_id", merchantId);
            }
        } else if ("admin".equals(role)) {
            // 管理员可以查看所有优惠券，不过滤
        }
        // 其他角色不允许查看
        
        wrapper.orderByDesc("create_time");
        List<Coupon> coupons = list(wrapper);
        
        // 填充商家名称（如果有merchantId）
        for (Coupon coupon : coupons) {
            if (coupon.getMerchantId() != null) {
                try {
                    com.fruitshop.entity.Merchant merchant = merchantMapper.selectById(coupon.getMerchantId());
                    if (merchant != null) {
                        coupon.setMerchantName(merchant.getShopName());
                    }
                } catch (Exception e) {
                    // 忽略错误，商家名称保持为null
                }
            }
        }
        
        return coupons;
    }

    @Override
    public void updateCouponStatus(Long id, Integer status) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }
        coupon.setStatus(status);
        couponMapper.updateById(coupon);
    }
}

