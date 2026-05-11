package com.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("reviews")
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long userId;
    private Long merchantId;
    private Long productId;
    private Integer rating;
    private String content;
    private String images;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String username;
    @TableField(exist = false)
    private String avatar; // 用户头像
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String merchantReply;
    @TableField(exist = false)
    private List<ReviewReply> replies; // 所有回复列表
    @TableField(exist = false)
    private String orderNo; // 订单号
    @TableField(exist = false)
    private String specName; // 购买规格
    @TableField(exist = false)
    private Integer quantity; // 购买数量
    @TableField(exist = false)
    private String productImage; // 商品图片
    @TableField(exist = false)
    private LocalDateTime orderCreateTime; // 订单创建时间（购买时间）
    @TableField(exist = false)
    private Integer likeCount; // 认同数
    @TableField(exist = false)
    private Integer dislikeCount; // 不认同数
    @TableField(exist = false)
    private Integer userLikeType; // 当前用户的点赞类型：1-认同，2-不认同，null-未点赞
}

