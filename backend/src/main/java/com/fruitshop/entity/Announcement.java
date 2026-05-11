package com.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("announcements")
public class Announcement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private Integer type; // 1-系统公告，2-活动公告，3-维护公告
    private Integer target; // 0-全部，1-用户，2-商家
    private Integer status; // 0-草稿，1-已发布，2-已下线
    private Integer isTop; // 0-否，1-是
    private Long adminId; // 发布管理员ID
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String adminName; // 发布管理员名称（展示用）
}

