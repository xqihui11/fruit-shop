package com.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("messages")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer type; // 消息类型：1-订单发货，2-商家回复评论，3-消费者回复评论，4-管理员回复留言
    private String title;
    private String content;
    private String linkUrl; // 跳转链接
    private Integer isRead; // 是否已读：0-未读，1-已读
    private LocalDateTime createTime;
    private LocalDateTime readTime;
}

