package com.buaa.clouddisk.module.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_file")
public class File {
    @TableId(type = IdType.AUTO)
    private Long fileId;
    private Long userId;
    private String filename;
    private String filePath; // 物理路径，下载核心字段
    private Integer isFolder; // 1=文件夹
    private Long fileSize;
    private String fileType;
    private Integer isDeleted;
}