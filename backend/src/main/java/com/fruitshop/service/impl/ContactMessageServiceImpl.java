package com.fruitshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitshop.common.UserContext;
import com.fruitshop.entity.ContactMessage;
import com.fruitshop.service.MessageService;
import com.fruitshop.mapper.ContactMessageMapper;
import com.fruitshop.service.ContactMessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContactMessageServiceImpl extends ServiceImpl<ContactMessageMapper, ContactMessage> implements ContactMessageService {

    private final MessageService messageService;

    public ContactMessageServiceImpl(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void createContactMessage(ContactMessage contactMessage) {
        // 如果当前有登录用户，则关联 userId，方便后续给该用户推送“管理员回复”消息
        Long userId = UserContext.getUserId();
        if (userId != null) {
            contactMessage.setUserId(userId);
        }
        contactMessage.setStatus(0); // 未处理
        contactMessage.setCreateTime(LocalDateTime.now());
        contactMessage.setUpdateTime(LocalDateTime.now());
        this.save(contactMessage);
    }

    @Override
    public List<ContactMessage> getAllMessages() {
        QueryWrapper<ContactMessage> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        return this.list(wrapper);
    }

    @Override
    public ContactMessage getMessageById(Long id) {
        return this.getById(id);
    }

    @Override
    public void updateMessageStatus(Long id, Integer status, String reply) {
        ContactMessage message = this.getById(id);
        if (message != null) {
            message.setStatus(status);
            message.setReply(reply);
            message.setUpdateTime(LocalDateTime.now());
            this.updateById(message);

            // 如果该留言关联了登录用户且管理员填写了回复，则给用户发送一条站内消息（类型4：管理员回复留言）
            if (message.getUserId() != null && reply != null && !reply.trim().isEmpty()) {
                String title = "管理员回复了您的留言";
                String content = reply.length() > 80 ? reply.substring(0, 80) + "..." : reply;
                // 跳转到在线留言页面，用户可以查看完整留言与回复
                String linkUrl = "/contact";
                messageService.sendMessage(message.getUserId(), 4, title, content, linkUrl);
            }
        }
    }
}

