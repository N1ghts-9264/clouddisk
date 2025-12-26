package com.buaa.clouddisk.module.share.dto;

import lombok.Data;

// 创建分享请求
@Data
public class ShareCreateDTO {
    private Long fileId;
    private Integer validDays; // 有效期天数 (0代表永久)
}
