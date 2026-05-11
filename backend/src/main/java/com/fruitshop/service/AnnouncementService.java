package com.fruitshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitshop.entity.Announcement;
import java.util.List;

public interface AnnouncementService extends IService<Announcement> {
    List<Announcement> getPublishedAnnouncements(Integer target); // 获取已发布的公告（target: 0-全部，1-用户，2-商家）
    Announcement createAnnouncement(Announcement announcement); // 创建公告（管理员）
    Announcement updateAnnouncement(Announcement announcement); // 更新公告（管理员）
    void deleteAnnouncement(Long id); // 删除公告（管理员）
    List<Announcement> getAllAnnouncements(); // 获取所有公告（管理员）
    void publishAnnouncement(Long id); // 发布公告
    void unpublishAnnouncement(Long id); // 下线公告
}

