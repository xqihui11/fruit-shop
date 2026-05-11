package com.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product_specs")
public class ProductSpec {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private String specName;
    private String specValue;
    private BigDecimal price;
    private BigDecimal originalPrice; // 原价，用于显示折扣
    private Integer stock;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

