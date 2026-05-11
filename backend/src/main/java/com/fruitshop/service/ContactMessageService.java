package com.fruitshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitshop.entity.ContactMessage;

import java.util.List;

public interface ContactMessageService extends IService<ContactMessage> {
    void createContactMessage(ContactMessage contactMessage);
    List<ContactMessage> getAllMessages();
    ContactMessage getMessageById(Long id);
    void updateMessageStatus(Long id, Integer status, String reply);
}

