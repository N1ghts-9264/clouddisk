package com.buaa.clouddisk.module.share.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分享信息实体类
 * 对应数据库表: sys_share
 */
@Data
@TableName("sys_share")
public class Share {
    @TableId(type = IdType.AUTO)
    private Long shareId;

    private Long fileId;

    private Long userId; // 分享者的ID

    private String code; // 4位提取码

    private Integer status; // 0=正常, 1=已失效

    private LocalDateTime expireTime; // 过期时间

    private LocalDateTime createTime;

    private Integer visitCount; // 浏览次数
}