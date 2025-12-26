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
    // 注意：不返回 filePath，防止泄露物理路径
}