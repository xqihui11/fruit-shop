package com.fruitshop.controller;

import com.fruitshop.common.Result;
import com.fruitshop.common.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fruitshop.entity.ChatConversation;
import com.fruitshop.entity.ChatMessage;
import com.fruitshop.mapper.ChatConversationMapper;
import com.fruitshop.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    /**
     * 约定：在聊天表中，user_id=0 代表“平台管理员”的虚拟用户。
     * 这样可以在不改动原有表结构的前提下，复用聊天服务实现“管理员 - 商家”的私聊。
     */
    private static final Long ADMIN_USER_ID = 0L;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatConversationMapper conversationMapper;

    /**
     * 用户侧：获取自己的会话列表
     */
    @GetMapping("/conversations")
    public Result<List<ChatConversation>> getUserConversations() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        List<ChatConversation> list = chatService.getUserConversations(userId);
        return Result.success(list);
    }

    /**
     * 用户侧：获取与某个商家的聊天记录
     */
    @GetMapping("/history")
    public Result<List<ChatMessage>> getUserHistory(@RequestParam Long merchantId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        List<ChatMessage> list = chatService.getMessagesForUser(userId, merchantId);
        // 进入会话即标记为已读
        chatService.markUserRead(userId, merchantId);
        return Result.success(list);
    }

    /**
     * 用户侧：发送消息给商家
     */
    @PostMapping("/send")
    public Result<ChatMessage> sendFromUser(@RequestBody Map<String, Object> body) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Long merchantId = body.get("merchantId") == null ? null : Long.valueOf(body.get("merchantId").toString());
        String content = body.get("content") != null ? body.get("content").toString() : null;
        String imageUrl = body.get("imageUrl") != null ? body.get("imageUrl").toString() : null;
        if ((content == null || content.trim().isEmpty()) && (imageUrl == null || imageUrl.trim().isEmpty())) {
            return Result.error("消息内容不能为空");
        }
        ChatMessage message = chatService.sendFromUser(userId, merchantId, content, imageUrl);
        return Result.success("发送成功", message);
    }

    /**
     * 商家侧：获取自己的会话列表
     */
    @GetMapping("/merchant/conversations")
    public Result<List<ChatConversation>> getMerchantConversations() {
        Long merchantId = UserContext.getUserId();
        if (merchantId == null) {
            return Result.error(401, "未登录");
        }
        List<ChatConversation> list = chatService.getMerchantConversations(merchantId);
        return Result.success(list);
    }

    /**
     * 商家侧：获取与指定用户的聊天记录
     */
    @GetMapping("/merchant/history")
    public Result<List<ChatMessage>> getMerchantHistory(@RequestParam Long userId) {
        Long merchantId = UserContext.getUserId();
        if (merchantId == null) {
            return Result.error(401, "未登录");
        }
        List<ChatMessage> list = chatService.getMessagesForMerchant(merchantId, userId);
        chatService.markMerchantRead(merchantId, userId);
        return Result.success(list);
    }

    /**
     * 商家侧：发送消息给用户
     */
    @PostMapping("/merchant/send")
    public Result<ChatMessage> sendFromMerchant(@RequestBody Map<String, Object> body) {
        Long merchantId = UserContext.getUserId();
        if (merchantId == null) {
            return Result.error(401, "未登录");
        }
        Long userId = body.get("userId") == null ? null : Long.valueOf(body.get("userId").toString());
        String content = body.get("content") != null ? body.get("content").toString() : null;
        String imageUrl = body.get("imageUrl") != null ? body.get("imageUrl").toString() : null;
        if ((content == null || content.trim().isEmpty()) && (imageUrl == null || imageUrl.trim().isEmpty())) {
            return Result.error("消息内容不能为空");
        }
        ChatMessage message = chatService.sendFromMerchant(merchantId, userId, content, imageUrl);
        return Result.success("发送成功", message);
    }

    /**
     * 用户显式标记会话已读（可用于轮询刷新后手动调用）
     */
    @PostMapping("/read")
    public Result<Void> markUserRead(@RequestBody Map<String, Object> body) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Long merchantId = body.get("merchantId") == null ? null : Long.valueOf(body.get("merchantId").toString());
        chatService.markUserRead(userId, merchantId);
        return Result.success("已标记为已读", null);
    }

    /**
     * 商家显式标记会话已读
     */
    @PostMapping("/merchant/read")
    public Result<Void> markMerchantRead(@RequestBody Map<String, Object> body) {
        Long merchantId = UserContext.getUserId();
        if (merchantId == null) {
            return Result.error(401, "未登录");
        }
        Long userId = body.get("userId") == null ? null : Long.valueOf(body.get("userId").toString());
        chatService.markMerchantRead(merchantId, userId);
        return Result.success("已标记为已读", null);
    }

    // ================== 管理员侧：与商家的私聊 ==================

    /**
     * 管理员：查看与所有商家的私聊会话列表
     * 会话表中 user_id = 0 代表“管理员虚拟用户”，merchant_id 为真实商家 ID。
     */
    @GetMapping("/admin/conversations")
    public Result<List<ChatConversation>> getAdminConversations() {
        String role = UserContext.getRole();
        if (!"admin".equals(role)) {
            return Result.error(403, "无权访问");
        }
        QueryWrapper<ChatConversation> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", ADMIN_USER_ID);
        wrapper.orderByDesc("last_time");
        List<ChatConversation> list = conversationMapper.selectList(wrapper);
        return Result.success(list);
    }

    /**
     * 管理员：获取与某个商家的聊天记录
     */
    @GetMapping("/admin/history")
    public Result<List<ChatMessage>> getAdminHistory(@RequestParam Long merchantId) {
        String role = UserContext.getRole();
        if (!"admin".equals(role)) {
            return Result.error(403, "无权访问");
        }
        List<ChatMessage> list = chatService.getMessagesForUser(ADMIN_USER_ID, merchantId);
        // 进入会话即标记为已读
        chatService.markUserRead(ADMIN_USER_ID, merchantId);
        return Result.success(list);
    }

    /**
     * 管理员：发送消息给商家
     */
    @PostMapping("/admin/send")
    public Result<ChatMessage> sendFromAdmin(@RequestBody Map<String, Object> body) {
        String role = UserContext.getRole();
        if (!"admin".equals(role)) {
            return Result.error(403, "无权发送");
        }
        Long merchantId = body.get("merchantId") == null ? null : Long.valueOf(body.get("merchantId").toString());
        String content = body.get("content") != null ? body.get("content").toString() : null;
        String imageUrl = body.get("imageUrl") != null ? body.get("imageUrl").toString() : null;
        if ((content == null || content.trim().isEmpty()) && (imageUrl == null || imageUrl.trim().isEmpty())) {
            return Result.error("消息内容不能为空");
        }
        ChatMessage message = chatService.sendFromUser(ADMIN_USER_ID, merchantId, content, imageUrl);
        // 将发送方类型标记为 ADMIN，便于前端区分展示（非 MERCHANT 即认为是对方）
        message.setSenderType("ADMIN");
        return Result.success("发送成功", message);
    }

    /**
     * 管理员：显式标记某个商家会话为已读
     */
    @PostMapping("/admin/read")
    public Result<Void> markAdminRead(@RequestBody Map<String, Object> body) {
        String role = UserContext.getRole();
        if (!"admin".equals(role)) {
            return Result.error(403, "无权操作");
        }
        Long merchantId = body.get("merchantId") == null ? null : Long.valueOf(body.get("merchantId").toString());
        chatService.markUserRead(ADMIN_USER_ID, merchantId);
        return Result.success("已标记为已读", null);
    }
}


