package com.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("review_replies")
public class ReviewReply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reviewId;
    private Long merchantId;
    private Long userId; // 用户ID（商家回复时为NULL，消费者回复时填写）
    private Integer replyType; // 回复类型：1-商家回复，2-消费者回复
    private Long replyToId; // 回复目标ID（回复某个回复时填写，直接回复评价时为NULL）
    private String content;
    private LocalDateTime createTime;
    
    @TableField(exist = false)
    private String username; // 回复者用户名（非数据库字段）
    @TableField(exist = false)
    private String merchantName; // 商家名称（非数据库字段）
    @TableField(exist = false)
    private String avatar; // 用户头像（非数据库字段）
    @TableField(exist = false)
    private String replyToUsername; // 回复目标用户名（非数据库字段）
    @TableField(exist = false)
    private String replyToMerchantName; // 回复目标商家名称（非数据库字段）
}

