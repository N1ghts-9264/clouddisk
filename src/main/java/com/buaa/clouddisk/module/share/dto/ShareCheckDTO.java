package com.buaa.clouddisk.module.share.dto;

import lombok.Data;

// 校验提取码请求
@Data
public class ShareCheckDTO {
    private Long shareId;
    private String code;
}

