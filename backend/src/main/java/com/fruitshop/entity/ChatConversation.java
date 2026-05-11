package com.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_conversation")
public class ChatConversation {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long merchantId;

    private String lastMessage;
    private LocalDateTime lastTime;

    private Integer userUnreadCount;
    private Integer merchantUnreadCount;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}



