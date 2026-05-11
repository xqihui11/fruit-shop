package com.fruitshop.controller;

import com.fruitshop.common.Result;
import com.fruitshop.common.UserContext;
import com.fruitshop.entity.ContactMessage;
import com.fruitshop.service.ContactMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contact")
public class ContactMessageController {
    @Autowired
    private ContactMessageService contactMessageService;

    @PostMapping("/message")
    public Result<Void> createMessage(@RequestBody ContactMessage contactMessage) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.error(401, "请先登录后再提交在线留言");
            }
            // userId 会在 Service 层写入 ContactMessage 中
            contactMessageService.createContactMessage(contactMessage);
            return Result.success("留言提交成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/admin/messages")
    public Result<List<ContactMessage>> getAllMessages() {
        try {
            List<ContactMessage> messages = contactMessageService.getAllMessages();
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/admin/message/{id}")
    public Result<ContactMessage> getMessageById(@PathVariable Long id) {
        try {
            ContactMessage message = contactMessageService.getMessageById(id);
            return Result.success(message);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/admin/message/{id}")
    public Result<Void> updateMessage(@PathVariable Long id, @RequestBody ContactMessage contactMessage) {
        try {
            contactMessageService.updateMessageStatus(id, contactMessage.getStatus(), contactMessage.getReply());
            return Result.success("更新成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

