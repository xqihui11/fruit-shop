package com.fruitshop.controller;

import com.fruitshop.common.Result;
import com.fruitshop.entity.Coupon;
import com.fruitshop.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coupon")
public class CouponController {
    @Autowired
    private CouponService couponService;

    // 用户端：获取可领取的优惠券列表
    @GetMapping("/available")
    public Result<List<Coupon>> getAvailableCoupons(@RequestParam(required = false) Long merchantId) {
        try {
            List<Coupon> coupons = couponService.getAvailableCoupons(merchantId);
            return Result.success(coupons);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 用户端：领取优惠券
    @PostMapping("/receive/{id}")
    public Result<Void> receiveCoupon(@PathVariable Long id) {
        try {
            couponService.receiveCoupon(id);
            return Result.success("领取成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 用户端：获取我的优惠券列表
    @GetMapping("/my")
    public Result<List<Coupon>> getMyCoupons(@RequestParam(required = false) Integer status) {
        try {
            List<Coupon> coupons = couponService.getUserCoupons(status);
            return Result.success(coupons);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 用户端：清除已使用或已过期的优惠券
    @DeleteMapping("/clear")
    public Result<Void> clearExpiredCoupons(@RequestParam Integer status) {
        try {
            couponService.clearExpiredCoupons(status);
            return Result.success("清除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

