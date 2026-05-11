package com.fruitshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitshop.common.UserContext;
import com.fruitshop.entity.Admin;
import com.fruitshop.entity.Announcement;
import com.fruitshop.mapper.AdminMapper;
import com.fruitshop.mapper.AnnouncementMapper;
import com.fruitshop.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {
    @Autowired
    private AnnouncementMapper announcementMapper;
    @Autowired
    private AdminMapper adminMapper;

    @Override
    public List<Announcement> getPublishedAnnouncements(Integer target) {
        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1); // 已发布
        if (target != null && target != 0) {
            wrapper.and(w -> w.eq("target", 0).or().eq("target", target)); // 全部或指定目标
        } else {
            wrapper.and(w -> w.eq("target", 0).or().eq("target", 1).or().eq("target", 2)); // 全部
        }
        wrapper.orderByDesc("is_top"); // 置顶优先
        wrapper.orderByDesc("publish_time");
        
        List<Announcement> announcements = list(wrapper);
        
        // 补充管理员名称
        for (Announcement announcement : announcements) {
            if (announcement.getAdminId() != null) {
                Admin admin = adminMapper.selectById(announcement.getAdminId());
                if (admin != null) {
                    announcement.setAdminName(admin.getUsername());
                }
            }
        }
        
        return announcements;
    }

    @Override
    public Announcement createAnnouncement(Announcement announcement) {
        Long adminId = UserContext.getUserId();
        if (adminId == null) {
            throw new RuntimeException("请先登录");
        }
        
        announcement.setAdminId(adminId);
        if (announcement.getStatus() == null) {
            announcement.setStatus(0); // 默认草稿
        }
        if (announcement.getIsTop() == null) {
            announcement.setIsTop(0);
        }
        if (announcement.getTarget() == null) {
            announcement.setTarget(0); // 默认全部
        }
        
        // 如果状态是已发布，设置发布时间
        if (announcement.getStatus() == 1) {
            announcement.setPublishTime(LocalDateTime.now());
        }
        
        announcementMapper.insert(announcement);
        return announcement;
    }

    @Override
    public Announcement updateAnnouncement(Announcement announcement) {
        Announcement existing = announcementMapper.selectById(announcement.getId());
        if (existing == null) {
            throw new RuntimeException("公告不存在");
        }
        
        // 如果状态从非发布变为发布，设置发布时间
        if (existing.getStatus() != 1 && announcement.getStatus() == 1) {
            announcement.setPublishTime(LocalDateTime.now());
        }
        
        announcementMapper.updateById(announcement);
        return announcement;
    }

    @Override
    public void deleteAnnouncement(Long id) {
        announcementMapper.deleteById(id);
    }

    @Override
    public List<Announcement> getAllAnnouncements() {
        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("is_top");
        wrapper.orderByDesc("create_time");
        
        List<Announcement> announcements = list(wrapper);
        
        // 补充管理员名称
        for (Announcement announcement : announcements) {
            if (announcement.getAdminId() != null) {
                Admin admin = adminMapper.selectById(announcement.getAdminId());
                if (admin != null) {
                    announcement.setAdminName(admin.getUsername());
                }
            }
        }
        
        return announcements;
    }

    @Override
    public void publishAnnouncement(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new RuntimeException("公告不存在");
        }
        announcement.setStatus(1);
        if (announcement.getPublishTime() == null) {
            announcement.setPublishTime(LocalDateTime.now());
        }
        announcementMapper.updateById(announcement);
    }

    @Override
    public void unpublishAnnouncement(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new RuntimeException("公告不存在");
        }
        announcement.setStatus(2); // 已下线
        announcementMapper.updateById(announcement);
    }
}

