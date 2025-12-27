package com.buaa.clouddisk.module.file.vo;

import lombok.Data;

@Data
public class FileVO {
    private Long fileId;
    private Long parentId;
    private String filename;
    private Boolean isFolder;
    private Long fileSize;
    private String fileType;
    private String filePath;
    private String identifier;
    private String createTime;
    private String updateTime;
}