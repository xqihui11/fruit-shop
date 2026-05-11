package com.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("after_sales")
public class AfterSale {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long orderItemId;
    private Long userId;
    private Long merchantId;
    private Integer type; // 1-退货，2-退款
    private String reason;
    private String description;
    private String images;
    private BigDecimal refundAmount;
    // 退货物流信息（仅针对退货退款）
    private String returnLogisticsCompany;
    private String returnLogisticsNo;
    private LocalDateTime returnLogisticsTime;
    private Integer status; // 0-待处理，1-已同意，2-已驳回，3-已完成
    private String merchantReply;
    private LocalDateTime replyTime;
    private LocalDateTime processTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String orderNo;
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String productImage;
}

