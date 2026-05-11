package com.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("merchants")
public class Merchant {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String shopName;
    private String shopDescription;
    /** 店铺地址 */
    private String shopAddress;
    /** 营业时间，如 09:00-21:00 */
    private String businessHours;
    /**
     * 营业状态：0-按营业时间自动判断，1-强制营业中，2-强制休息中
     */
    @TableField("business_status")
    private Integer businessStatus;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String businessLicense;
    private String avatar; // 商家头像
    private Integer status;
    private LocalDateTime auditTime;
    private Long auditAdminId;
    private LocalDateTime registerTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 管理员端统计字段（不映射到表）
     */
    @TableField(exist = false)
    private Long productCount;

    @TableField(exist = false)
    private Long orderCount;

    /**
     * 最后活跃时间（近似为最近一笔订单时间，仅用于展示）
     */
    @TableField(exist = false)
    private LocalDateTime lastLoginTime;
}

