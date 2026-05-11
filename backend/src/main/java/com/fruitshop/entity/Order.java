package com.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long merchantId;
    private Long addressId;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private String paymentMethod;
    private Integer paymentStatus;
    private LocalDateTime paymentTime;
    private Integer orderStatus;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private LocalDateTime shipTime;
    private String logisticsCompany;
    private String logisticsNo;
    private LocalDateTime receiveTime;
    private LocalDateTime completeTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String firstProductName;

    @TableField(exist = false)
    private String firstProductImage;

    @TableField(exist = false)
    private Integer totalQuantity;

    @TableField(exist = false)
    private String receiverName;

    @TableField(exist = false)
    private String receiverPhone;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String merchantName;

    @TableField(exist = false)
    private Boolean afterSaleInProgress;

    /**
     * 商家头像（仅用于前端展示，不映射到表）
     */
    @TableField(exist = false)
    private String merchantAvatar;
}

