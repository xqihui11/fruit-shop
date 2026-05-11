package com.fruitshop.controller;

import com.fruitshop.common.Result;
import com.fruitshop.entity.Announcement;
import com.fruitshop.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/announcement")
public class AnnouncementController {
    @Autowired
    private AnnouncementService announcementService;

    // 用户端/商家端：获取已发布的公告列表
    @GetMapping("/published")
    public Result<List<Announcement>> getPublishedAnnouncements(@RequestParam(required = false) Integer target) {
        try {
            List<Announcement> announcements = announcementService.getPublishedAnnouncements(target);
            return Result.success(announcements);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

