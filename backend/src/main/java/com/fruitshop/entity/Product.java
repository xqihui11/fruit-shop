package com.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("products")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long merchantId;
    private Long categoryId;
    private String name;
    private String description;
    private String mainImage;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer salesCount;
    private Integer freshnessLevel;
    private Integer status;
    private LocalDateTime auditTime;
    private Long auditAdminId;
    private String auditRemark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 特价配置（商家可编辑）
     * specialEnabled: 0-否 1-是
     * specialType: DISCOUNT / BUY_N_GET_M
     */
    private Integer specialEnabled;
    private String specialType;

    /** 折扣（0~1），如 0.8 表示 8 折 */
    private BigDecimal specialDiscount;

    /** 买N送M，如 买2送1 -> specialBuy=2, specialFree=1 */
    private Integer specialBuy;
    private Integer specialFree;

    /** 前端展示用标签（可选），如“8折”“买二送一” */
    private String specialLabel;

    @TableField(exist = false)
    private String merchantName;

    @TableField(exist = false)
    private Long reviewCount;
}

