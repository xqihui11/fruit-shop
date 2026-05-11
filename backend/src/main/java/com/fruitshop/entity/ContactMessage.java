package com.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("contact_messages")
public class ContactMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name; // 姓名
    private String phone; // 联系电话
    private String email; // 邮箱
    private String type; // 留言类型
    private String content; // 留言内容
    private Integer status; // 状态：0-未处理，1-处理中，2-已处理
    private String reply; // 回复内容
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 可选：如果用户在登录状态下提交在线留言，则记录 userId，方便后续给该用户推送“管理员回复”消息
     */
    private Long userId;
}

