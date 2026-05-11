package com.fruitshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitshop.common.UserContext;
import com.fruitshop.entity.Message;
import com.fruitshop.mapper.MessageMapper;
import com.fruitshop.service.MessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Override
    public void sendMessage(Long userId, Integer type, String title, String content, String linkUrl) {
        try {
            Message message = new Message();
            message.setUserId(userId);
            message.setType(type);
            message.setTitle(title);
            message.setContent(content);
            message.setLinkUrl(linkUrl);
            message.setIsRead(0);
            message.setCreateTime(LocalDateTime.now());
            this.save(message);
            System.out.println("消息发送成功: userId=" + userId + ", type=" + type + ", title=" + title);
        } catch (Exception e) {
            System.err.println("消息发送失败: userId=" + userId + ", type=" + type + ", error=" + e.getMessage());
            e.printStackTrace();
            // 不抛出异常，避免影响主流程
        }
    }

    @Override
    public List<Message> getUserMessages(Integer type) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (type != null) {
            wrapper.eq("type", type);
        }
        wrapper.orderByDesc("create_time");
        return this.list(wrapper);
    }

    @Override
    public void markAsRead(Long messageId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        Message message = this.getById(messageId);
        if (message != null && message.getUserId().equals(userId)) {
            message.setIsRead(1);
            message.setReadTime(LocalDateTime.now());
            this.updateById(message);
        }
    }

    @Override
    public void markAllAsRead() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("is_read", 0);

        List<Message> messages = this.list(wrapper);
        LocalDateTime now = LocalDateTime.now();
        for (Message message : messages) {
            message.setIsRead(1);
            message.setReadTime(now);
        }
        if (!messages.isEmpty()) {
            this.updateBatchById(messages);
        }
    }

    @Override
    public Integer getUnreadCount() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return 0;
        }

        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("is_read", 0);
        return (int) this.count(wrapper);
    }
}

