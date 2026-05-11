package com.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("coupons")
public class Coupon {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer type; // 1-满减券，2-折扣券
    private BigDecimal discountAmount; // 优惠金额（满减券）
    private BigDecimal discountRate; // 折扣率（折扣券，如0.9表示9折）
    private BigDecimal minAmount; // 最低使用金额
    private BigDecimal maxDiscountAmount; // 最大优惠金额（折扣券）
    private Integer totalCount; // 发放总数，0表示不限制
    private Integer receivedCount; // 已领取数量
    private Integer usedCount; // 已使用数量
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime validStartTime; // 有效期开始时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime validEndTime; // 有效期结束时间
    private Integer status; // 0-已下架，1-进行中，2-已结束
    private String description;
    private Long merchantId; // 商家ID，NULL表示平台优惠券（全平台可用），有值表示商家优惠券（仅该商家可用）
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Boolean canReceive; // 是否可以领取（前端展示用）
    
    @TableField(exist = false)
    private String merchantName; // 商家名称（前端展示用）
}

