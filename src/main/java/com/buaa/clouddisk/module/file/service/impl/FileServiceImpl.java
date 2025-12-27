package com.buaa.clouddisk.module.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buaa.clouddisk.common.util.FileUtil;
import com.buaa.clouddisk.common.util.Md5Util;
import com.buaa.clouddisk.module.file.entity.SysFile;
import com.buaa.clouddisk.module.file.mapper.SysFileMapper;
import com.buaa.clouddisk.module.file.service.IFileService;
import com.buaa.clouddisk.module.storage.StorageEngine;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("fileService")
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements IFileService {

    private final StorageEngine storageEngine;

    @Override
    public Long uploadFile(MultipartFile file, Long parentId, Long userId) {
        String originalName = file.getOriginalFilename();
        
        // 1. 格式校验 (角色B 核心任务 4)
        if (FileUtil.isDangerousFile(originalName)) {
            throw new RuntimeException("禁止上传危险格式文件！");
        }

        // 2. 计算 MD5 (角色B 核心任务 3)
        String md5 = Md5Util.getFileMd5(file);

        // 3. 秒传/去重逻辑：查询库里是否已经有这个 MD5
        LambdaQueryWrapper<SysFile> query = new LambdaQueryWrapper<>();
        query.eq(SysFile::getIdentifier, md5).eq(SysFile::getIsDeleted, false);
        SysFile existFile = this.getOne(query, false);

        String finalPath;
        if (existFile != null) {
            // 秒传成功：直接复用已有的物理路径，不写磁盘
            finalPath = existFile.getFilePath();
        } else {
            // 秒传失败：调用存储引擎写磁盘
            try {
                finalPath = storageEngine.store(file, userId);
            } catch (Exception e) {
                throw new RuntimeException("文件物理存储失败");
            }
        }

        // 4. 落库 (存入 sys_file 表)
        SysFile sysFile = new SysFile();
        sysFile.setUserId(userId);
        sysFile.setParentId(parentId);
        sysFile.setFilename(originalName); // 这里存的是显示名
        sysFile.setIsFolder(false);
        sysFile.setFileSize(file.getSize());
        sysFile.setFileType(FileUtil.getSuffix(originalName));
        sysFile.setFilePath(finalPath);
        sysFile.setIdentifier(md5);
        sysFile.setIsDeleted(false);
        sysFile.setCreateTime(LocalDateTime.now());
        sysFile.setUpdateTime(LocalDateTime.now());
        sysFile.setIsDeleted(false);    
        this.save(sysFile);
        return sysFile.getFileId(); // 返回新文件的 ID
    }
}