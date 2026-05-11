package com.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_coupons")
public class UserCoupon {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long couponId;
    private Integer status; // 0-未使用，1-已使用，2-已过期
    private Long orderId; // 使用的订单ID
    private LocalDateTime usedTime;
    private LocalDateTime receiveTime;
    private LocalDateTime expireTime;

    @TableField(exist = false)
    private Coupon coupon; // 关联的优惠券信息
}

