package com.fruitshop.service;

import com.fruitshop.entity.ChatConversation;
import com.fruitshop.entity.ChatMessage;

import java.util.List;

public interface ChatService {

    ChatConversation getOrCreateConversation(Long userId, Long merchantId);

    List<ChatConversation> getUserConversations(Long userId);

    List<ChatConversation> getMerchantConversations(Long merchantId);

    List<ChatMessage> getMessagesForUser(Long userId, Long merchantId);

    List<ChatMessage> getMessagesForMerchant(Long merchantId, Long userId);

    ChatMessage sendFromUser(Long userId, Long merchantId, String content, String imageUrl);

    ChatMessage sendFromMerchant(Long merchantId, Long userId, String content, String imageUrl);

    void markUserRead(Long userId, Long merchantId);

    void markMerchantRead(Long merchantId, Long userId);
}



