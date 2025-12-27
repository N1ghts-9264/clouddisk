package com.buaa.clouddisk.module.file.vo;

import lombok.Data;

@Data
public class SpaceInfo {
    private Long usedSize; // 已使用空间(字节)
    private Long totalSize; // 总容量(字节)
}