package com.fruitshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitshop.entity.Message;

import java.util.List;

public interface MessageService extends IService<Message> {
    /**
     * 发送消息
     */
    void sendMessage(Long userId, Integer type, String title, String content, String linkUrl);

    /**
     * 获取用户消息列表
     */
    List<Message> getUserMessages(Integer type);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long messageId);

    /**
     * 标记所有消息为已读
     */
    void markAllAsRead();

    /**
     * 获取未读消息数量
     */
    Integer getUnreadCount();
}

