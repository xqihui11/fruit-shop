package com.fruitshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitshop.entity.Coupon;
import java.util.List;

public interface CouponService extends IService<Coupon> {
    List<Coupon> getAvailableCoupons(); // 获取可领取的优惠券列表（所有平台优惠券和商家优惠券）
    List<Coupon> getAvailableCoupons(Long merchantId); // 获取可领取的优惠券列表（平台优惠券+指定商家优惠券）
    void receiveCoupon(Long couponId); // 领取优惠券
    List<Coupon> getUserCoupons(Integer status); // 获取用户的优惠券列表（0-未使用，1-已使用，2-已过期）
    void clearExpiredCoupons(Integer status); // 清除已使用或已过期的优惠券（status: 1-已使用，2-已过期）
    Coupon createCoupon(Coupon coupon); // 创建优惠券（管理员）
    Coupon updateCoupon(Coupon coupon); // 更新优惠券（管理员）
    void deleteCoupon(Long id); // 删除优惠券（管理员）
    List<Coupon> getAllCoupons(); // 获取所有优惠券（管理员查看所有，商家查看自己的）
    void updateCouponStatus(Long id, Integer status); // 更新优惠券状态
}

