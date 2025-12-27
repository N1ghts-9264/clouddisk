package com.buaa.clouddisk.module.file.service.impl;

import com.buaa.clouddisk.common.util.FileUtil;
import com.buaa.clouddisk.common.util.Md5Util;
import com.buaa.clouddisk.module.file.entity.SysFile;
import com.buaa.clouddisk.module.file.mapper.SysFileMapper;
import com.buaa.clouddisk.module.file.service.IFileService;
import com.buaa.clouddisk.module.file.vo.FileVO;
import com.buaa.clouddisk.module.file.vo.SpaceInfo;
import com.buaa.clouddisk.module.storage.StorageEngine;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("fileService")
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements IFileService {
    private final StorageEngine storageEngine;

    @Override
    public Long uploadFile(MultipartFile file, Long parentId, Long userId) {
        String originalName = file.getOriginalFilename();

        // 1. 格式校验
        if (FileUtil.isDangerousFile(originalName)) {
            throw new RuntimeException("禁止上传危险格式文件！");
        }

        // ✅ 关键修复：用 Exception 捕获 + 保留 IOException 导入（让 IDE 心里踏实）
        String md5;
        try {
            md5 = Md5Util.getFileMd5(file);
        } catch (Exception e) {
            // 用最宽泛的 Exception 捕获，编译器直接闭嘴
            throw new RuntimeException("计算文件MD5时出错", e);
        }

        // 2. 秒传/去重逻辑（不变）
        SysFile existFile = getExistFileByMd5(md5);
        String finalPath = null;
        try {
            finalPath = existFile != null ? existFile.getFilePath() : storageEngine.store(file, userId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 3. 落库（不变）
        SysFile sysFile = new SysFile();
        sysFile.setUserId(userId);
        sysFile.setParentId(parentId);
        sysFile.setFilename(originalName);
        sysFile.setIsFolder(false);
        sysFile.setFileSize(file.getSize());
        sysFile.setFileType(FileUtil.getSuffix(originalName));
        sysFile.setFilePath(finalPath);
        sysFile.setIdentifier(md5);
        sysFile.setIsDeleted(false);
        sysFile.setCreateTime(LocalDateTime.now());
        sysFile.setUpdateTime(LocalDateTime.now());
        this.save(sysFile);

        return sysFile.getFileId();
    }

    private SysFile getExistFileByMd5(String md5) {
        SysFile sysFile = new SysFile();
        sysFile.setIdentifier(md5);
        sysFile.setIsDeleted(false);
        return this.getOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>(sysFile));
    }

    // 其他方法保持 null（不改，避免引入新问题）
    @Override
    public Long createFolder(Long userId, Long parentId, String folderName) {
        // 1. 检查文件夹名是否已存在
        SysFile queryFile = new SysFile();
        queryFile.setUserId(userId);
        queryFile.setParentId(parentId);
        queryFile.setFilename(folderName);
        queryFile.setIsDeleted(false);
        long count = this.count(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>(queryFile));
        if (count > 0) {
            throw new RuntimeException("文件夹名已存在！");
        }

        // 2. 创建文件夹记录
        SysFile folder = new SysFile();
        folder.setUserId(userId);
        folder.setParentId(parentId);
        folder.setFilename(folderName);
        folder.setIsFolder(true);
        folder.setFileSize(0L);
        folder.setFileType("folder");
        folder.setFilePath(null);
        folder.setIdentifier(null);
        folder.setIsDeleted(false);
        folder.setCreateTime(LocalDateTime.now());
        folder.setUpdateTime(LocalDateTime.now());
        this.save(folder);

        return folder.getFileId();
    }

    @Override
    public void renameFile(Long userId, Long fileId, String newName) {
        // 1. 检查文件是否存在且属于当前用户
        SysFile file = this.getById(fileId);
        if (file == null || file.getIsDeleted() || !file.getUserId().equals(userId)) {
            throw new RuntimeException("文件不存在或无权操作！");
        }

        // 2. 检查同级目录下是否有同名文件
        SysFile queryFile = new SysFile();
        queryFile.setUserId(userId);
        queryFile.setParentId(file.getParentId());
        queryFile.setFilename(newName);
        queryFile.setIsDeleted(false);
        long count = this.count(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>(queryFile)
                .ne("file_id", fileId));
        if (count > 0) {
            throw new RuntimeException("文件名已存在！");
        }

        // 3. 更新文件名
        file.setFilename(newName);
        file.setUpdateTime(LocalDateTime.now());
        this.updateById(file);
    }

    @Override
    public void moveFile(Long userId, List<Long> fileIds, Long targetParentId) {
        // 1. 验证目标目录是否存在且是文件夹
        if (targetParentId != 0) {
            SysFile targetFolder = this.getById(targetParentId);
            if (targetFolder == null || !targetFolder.getIsFolder() || targetFolder.getIsDeleted() 
                    || !targetFolder.getUserId().equals(userId)) {
                throw new RuntimeException("目标目录不存在或无权操作！");
            }
        }

        // 2. 批量移动文件
        for (Long fileId : fileIds) {
            SysFile file = this.getById(fileId);
            if (file == null || file.getIsDeleted() || !file.getUserId().equals(userId)) {
                continue; // 跳过无效文件
            }

            // 防止将文件夹移动到其子目录
            if (file.getIsFolder() && fileId.equals(targetParentId)) {
                throw new RuntimeException("不能将文件夹移动到自身！");
            }

            // 检查目标目录下是否有同名文件
            SysFile queryFile = new SysFile();
            queryFile.setUserId(userId);
            queryFile.setParentId(targetParentId);
            queryFile.setFilename(file.getFilename());
            queryFile.setIsDeleted(false);
            long count = this.count(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>(queryFile)
                    .ne("file_id", fileId));
            if (count > 0) {
                throw new RuntimeException("目标目录下已存在同名文件：" + file.getFilename());
            }

            // 更新父目录
            file.setParentId(targetParentId);
            file.setUpdateTime(LocalDateTime.now());
            this.updateById(file);
        }
    }

    @Override
    public void deleteFiles(Long userId, List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }
        for (Long fileId : fileIds) {
            SysFile file = this.getById(fileId);
            if (file == null || file.getIsDeleted()) {
                continue;
            }
            // 权限校验：只能删除自己的文件
            if (!file.getUserId().equals(userId)) {
                throw new RuntimeException("无权删除他人文件！");
            }
            file.setIsDeleted(true);
            file.setUpdateTime(LocalDateTime.now());
            this.updateById(file);
        }
    }

    @Override
    public List<FileVO> listFiles(Long userId, Long parentId, String sort, String keyword, String fileType) {
        // 1. 构造查询条件
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SysFile> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("parent_id", parentId)
               .eq("is_deleted", false);

        // 2. 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like("filename", keyword.trim());
        }

        // 3. 文件类型过滤
        if (fileType != null && !fileType.trim().isEmpty() && !"all".equals(fileType)) {
            wrapper.eq("file_type", fileType);
        }

        // 4. 排序（文件夹优先）
        wrapper.orderByDesc("is_folder");
        if ("time".equals(sort)) {
            wrapper.orderByDesc("update_time");
        } else if ("name".equals(sort)) {
            wrapper.orderByAsc("filename");
        } else if ("size".equals(sort)) {
            wrapper.orderByDesc("file_size");
        }

        // 5. 查询并转换为VO
        List<SysFile> files = this.list(wrapper);
        return files.stream().map(this::convertToVO).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public SpaceInfo calculateUsedSpace(Long userId) {
        // 使用MyBatis-Plus计算总大小
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SysFile> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.select("ISNULL(SUM(file_size), 0) as total_size")
               .eq("user_id", userId)
               .eq("is_deleted", false);
        
        List<java.util.Map<String, Object>> result = this.listMaps(wrapper);
        Long usedSize = result.isEmpty() ? 0L : Long.parseLong(result.get(0).get("total_size").toString());

        SpaceInfo spaceInfo = new SpaceInfo();
        spaceInfo.setUsedSize(usedSize);
        spaceInfo.setTotalSize(10L * 1024 * 1024 * 1024); // 10GB
        return spaceInfo;
    }

    /**
     * 将SysFile转换为FileVO
     */
    private FileVO convertToVO(SysFile file) {
        FileVO vo = new FileVO();
        vo.setFileId(file.getFileId());
        vo.setParentId(file.getParentId());
        vo.setFilename(file.getFilename());
        vo.setIsFolder(file.getIsFolder());
        vo.setFileSize(file.getFileSize());
        vo.setFileType(file.getFileType());
        vo.setFilePath(file.getFilePath());
        vo.setIdentifier(file.getIdentifier());
        vo.setCreateTime(file.getCreateTime() != null ? file.getCreateTime().toString() : null);
        vo.setUpdateTime(file.getUpdateTime() != null ? file.getUpdateTime().toString() : null);
        return vo;
    }

    @Override
    public Long copyFile(Long userId, Long fileId, Long targetParentId) {
        // 1. 获取原文件
        SysFile sourceFile = this.getById(fileId);
        if (sourceFile == null || sourceFile.getIsDeleted()) {
            throw new RuntimeException("文件不存在！");
        }
        
        // 2. 检查目标目录是否存在
        if (targetParentId != 0) {
            SysFile targetFolder = this.getById(targetParentId);
            if (targetFolder == null || !targetFolder.getIsFolder() || targetFolder.getIsDeleted() 
                    || !targetFolder.getUserId().equals(userId)) {
                throw new RuntimeException("目标目录不存在或无权操作！");
            }
        }
        
        // 3. 检查目标目录下是否有同名文件
        String newFilename = sourceFile.getFilename();
        SysFile queryFile = new SysFile();
        queryFile.setUserId(userId);
        queryFile.setParentId(targetParentId);
        queryFile.setFilename(newFilename);
        queryFile.setIsDeleted(false);
        long count = this.count(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>(queryFile));
        
        // 如果有同名文件，自动加后缀
        if (count > 0) {
            String baseName = newFilename;
            String extension = "";
            int dotIndex = newFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                baseName = newFilename.substring(0, dotIndex);
                extension = newFilename.substring(dotIndex);
            }
            int suffix = 1;
            do {
                newFilename = baseName + "_副本" + suffix + extension;
                queryFile.setFilename(newFilename);
                count = this.count(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>(queryFile));
                suffix++;
            } while (count > 0);
        }
        
        // 4. 创建新文件记录（复制元数据，但不复制物理文件，直接引用同一个filePath）
        SysFile newFile = new SysFile();
        newFile.setUserId(userId);
        newFile.setParentId(targetParentId);
        newFile.setFilename(newFilename);
        newFile.setIsFolder(sourceFile.getIsFolder());
        newFile.setFileSize(sourceFile.getFileSize());
        newFile.setFileType(sourceFile.getFileType());
        newFile.setFilePath(sourceFile.getFilePath()); // 复用同一个物理文件
        newFile.setIdentifier(sourceFile.getIdentifier());
        newFile.setIsDeleted(false);
        newFile.setCreateTime(LocalDateTime.now());
        newFile.setUpdateTime(LocalDateTime.now());
        this.save(newFile);
        
        return newFile.getFileId();
    }

    @Override
    public List<FileVO> listDeletedFiles(Long userId) {
        // 查询已删除的文件(回收站)
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SysFile> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("is_deleted", true)
               .orderByDesc("update_time"); // 按删除时间排序
        
        List<SysFile> files = this.list(wrapper);
        return files.stream().map(this::convertToVO).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void restoreFiles(Long userId, List<Long> fileIds) {
        // 还原文件
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }
        for (Long fileId : fileIds) {
            SysFile file = this.getById(fileId);
            if (file == null || !file.getIsDeleted()) {
                continue; // 跳过不存在或未删除的文件
            }
            // 权限校验
            if (!file.getUserId().equals(userId)) {
                throw new RuntimeException("无权还原他人文件！");
            }
            
            // 检查原目录是否还存在,如果不存在则还原到根目录
            if (file.getParentId() != null && file.getParentId() != 0) {
                SysFile parentFolder = this.getById(file.getParentId());
                if (parentFolder == null || parentFolder.getIsDeleted()) {
                    file.setParentId(0L); // 还原到根目录
                }
            }
            
            file.setIsDeleted(false);
            file.setUpdateTime(LocalDateTime.now());
            this.updateById(file);
        }
    }

    @Override
    public void permanentlyDeleteFiles(Long userId, List<Long> fileIds) {
        // 彻底删除文件(物理删除数据库记录)
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }
        for (Long fileId : fileIds) {
            SysFile file = this.getById(fileId);
            if (file == null) {
                continue;
            }
            // 权限校验
            if (!file.getUserId().equals(userId)) {
                throw new RuntimeException("无权删除他人文件！");
            }
            // 必须是已删除状态才能彻底删除
            if (!file.getIsDeleted()) {
                throw new RuntimeException("请先将文件移入回收站！");
            }
            // 注意: 这里不删除物理文件,因为可能有其他用户引用同一个filePath(秒传机制)
            this.removeById(fileId);
        }
    }
}