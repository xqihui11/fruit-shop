package com.fruitshop.controller;

import com.fruitshop.common.Result;
import com.fruitshop.entity.Message;
import com.fruitshop.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/message")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @GetMapping("/list")
    public Result<List<Message>> getMessages(@RequestParam(required = false) Integer type) {
        try {
            List<Message> messages = messageService.getUserMessages(type);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable Long id) {
        try {
            messageService.markAsRead(id);
            return Result.success("标记成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/read-all")
    public Result<Void> markAllAsRead() {
        try {
            messageService.markAllAsRead();
            return Result.success("全部标记为已读", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Object>> getUnreadCount() {
        try {
            Integer count = messageService.getUnreadCount();
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

