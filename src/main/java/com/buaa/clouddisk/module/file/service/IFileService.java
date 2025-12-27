package com.buaa.clouddisk.module.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buaa.clouddisk.module.file.entity.SysFile;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService extends IService<SysFile> {
    // 核心上传接口
    Long uploadFile(MultipartFile file, Long parentId, Long userId);
}