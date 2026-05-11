package com.fruitshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fruitshop.entity.ChatConversation;
import com.fruitshop.entity.ChatMessage;
import com.fruitshop.mapper.ChatConversationMapper;
import com.fruitshop.mapper.ChatMessageMapper;
import com.fruitshop.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatConversationMapper conversationMapper;

    @Autowired
    private ChatMessageMapper messageMapper;

    @Override
    @Transactional
    public ChatConversation getOrCreateConversation(Long userId, Long merchantId) {
        QueryWrapper<ChatConversation> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("merchant_id", merchantId);
        ChatConversation conversation = conversationMapper.selectOne(wrapper);
        if (conversation == null) {
            conversation = new ChatConversation();
            conversation.setUserId(userId);
            conversation.setMerchantId(merchantId);
            conversation.setUserUnreadCount(0);
            conversation.setMerchantUnreadCount(0);
            conversation.setCreateTime(LocalDateTime.now());
            conversation.setUpdateTime(LocalDateTime.now());
            conversationMapper.insert(conversation);
        }
        return conversation;
    }

    @Override
    public List<ChatConversation> getUserConversations(Long userId) {
        QueryWrapper<ChatConversation> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("last_time");
        return conversationMapper.selectList(wrapper);
    }

    @Override
    public List<ChatConversation> getMerchantConversations(Long merchantId) {
        QueryWrapper<ChatConversation> wrapper = new QueryWrapper<>();
        wrapper.eq("merchant_id", merchantId);
        wrapper.orderByDesc("last_time");
        return conversationMapper.selectList(wrapper);
    }

    @Override
    public List<ChatMessage> getMessagesForUser(Long userId, Long merchantId) {
        ChatConversation conversation = getOrCreateConversation(userId, merchantId);
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("conversation_id", conversation.getId());
        wrapper.orderByAsc("create_time");
        return messageMapper.selectList(wrapper);
    }

    @Override
    public List<ChatMessage> getMessagesForMerchant(Long merchantId, Long userId) {
        ChatConversation conversation = getOrCreateConversation(userId, merchantId);
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("conversation_id", conversation.getId());
        wrapper.orderByAsc("create_time");
        return messageMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public ChatMessage sendFromUser(Long userId, Long merchantId, String content, String imageUrl) {
        ChatConversation conversation = getOrCreateConversation(userId, merchantId);

        ChatMessage message = new ChatMessage();
        message.setConversationId(conversation.getId());
        message.setUserId(userId);
        message.setMerchantId(merchantId);
        message.setSenderType("USER");
        message.setContent(content);
        message.setImageUrl(imageUrl);
        message.setIsReadForUser(1);
        message.setIsReadForMerchant(0);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);

        // 更新会话
        conversation.setLastMessage(buildLastMessagePreview(content, imageUrl));
        conversation.setLastTime(message.getCreateTime());
        Integer unread = conversation.getMerchantUnreadCount();
        conversation.setMerchantUnreadCount(unread == null ? 1 : unread + 1);
        conversation.setUpdateTime(LocalDateTime.now());
        conversationMapper.updateById(conversation);

        return message;
    }

    @Override
    @Transactional
    public ChatMessage sendFromMerchant(Long merchantId, Long userId, String content, String imageUrl) {
        ChatConversation conversation = getOrCreateConversation(userId, merchantId);

        ChatMessage message = new ChatMessage();
        message.setConversationId(conversation.getId());
        message.setUserId(userId);
        message.setMerchantId(merchantId);
        message.setSenderType("MERCHANT");
        message.setContent(content);
        message.setImageUrl(imageUrl);
        message.setIsReadForUser(0);
        message.setIsReadForMerchant(1);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);

        // 更新会话
        conversation.setLastMessage(buildLastMessagePreview(content, imageUrl));
        conversation.setLastTime(message.getCreateTime());
        Integer unread = conversation.getUserUnreadCount();
        conversation.setUserUnreadCount(unread == null ? 1 : unread + 1);
        conversation.setUpdateTime(LocalDateTime.now());
        conversationMapper.updateById(conversation);

        return message;
    }

    @Override
    @Transactional
    public void markUserRead(Long userId, Long merchantId) {
        ChatConversation conversation = getOrCreateConversation(userId, merchantId);

        // 清零会话中的用户未读数
        UpdateWrapper<ChatConversation> uw = new UpdateWrapper<>();
        uw.eq("id", conversation.getId())
                .set("user_unread_count", 0)
                .set("update_time", LocalDateTime.now());
        conversationMapper.update(null, uw);

        // 将该会话中属于用户的未读消息标记为已读
        UpdateWrapper<ChatMessage> msgUw = new UpdateWrapper<>();
        msgUw.eq("conversation_id", conversation.getId())
                .eq("is_read_for_user", 0)
                .set("is_read_for_user", 1);
        messageMapper.update(null, msgUw);
    }

    @Override
    @Transactional
    public void markMerchantRead(Long merchantId, Long userId) {
        ChatConversation conversation = getOrCreateConversation(userId, merchantId);

        UpdateWrapper<ChatConversation> uw = new UpdateWrapper<>();
        uw.eq("id", conversation.getId())
                .set("merchant_unread_count", 0)
                .set("update_time", LocalDateTime.now());
        conversationMapper.update(null, uw);

        UpdateWrapper<ChatMessage> msgUw = new UpdateWrapper<>();
        msgUw.eq("conversation_id", conversation.getId())
                .eq("is_read_for_merchant", 0)
                .set("is_read_for_merchant", 1);
        messageMapper.update(null, msgUw);
    }

    /**
     * 会话列表中的最后一条消息预览：
     * - 商品卡片统一展示为「[商品]」
     * - 纯文本展示前若干字符
     * - 仅图片展示为「[图片]」
     */
    private String buildLastMessagePreview(String content, String imageUrl) {
        if (content != null && !content.isEmpty()) {
            if (content.startsWith("__PRODUCT_CARD__")) {
                return "[商品]";
            }
            if (content.startsWith("__ORDER_CARD__")) {
                return "[订单]";
            }
            String text = content.trim();
            if (text.length() > 30) {
                text = text.substring(0, 30) + "...";
            }
            return text;
        }
        if (imageUrl != null && !imageUrl.isEmpty()) {
            return "[图片]";
        }
        return "";
    }
}



