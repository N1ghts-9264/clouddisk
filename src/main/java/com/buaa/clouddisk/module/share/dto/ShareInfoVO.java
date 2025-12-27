package com.buaa.clouddisk.module.share.dto;

import lombok.Data;

// 分享详情返回 (VO)
@Data
public class ShareInfoVO {
    private Long shareId;
    private Long fileId;
    private String filename;
    private Long fileSize;
    private String shareUser; // 分享者昵称/用户名
    private boolean expired;  // 是否过期
    
    // 新增字段：用于"我的分享"列表
    private String code;      // 提取码
    private String createTime; // 创建时间（格式化后的字符串）
    private String expireTime; // 过期时间（格式化后的字符串）
    // 注意：不返回 filePath，防止泄露物理路径
}