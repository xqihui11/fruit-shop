package com.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long conversationId;
    private Long userId;
    private Long merchantId;

    /**
     * 发送方类型：USER / MERCHANT
     */
    private String senderType;

    private String content;
    private String imageUrl;

    private Integer isReadForUser;
    private Integer isReadForMerchant;

    private LocalDateTime createTime;
}



