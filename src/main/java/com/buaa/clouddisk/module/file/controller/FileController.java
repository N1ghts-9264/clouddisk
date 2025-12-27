package com.buaa.clouddisk.module.file.controller;

import com.buaa.clouddisk.common.result.Result;
import com.buaa.clouddisk.common.util.UserContext;
import com.buaa.clouddisk.module.file.service.IFileService;
import com.buaa.clouddisk.module.file.vo.FileVO;
import com.buaa.clouddisk.module.file.vo.SpaceInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "文件管理")
@RestController
@RequestMapping("/api/file")
public class FileController {
    @Autowired
    private IFileService fileService;

    @Operation(description = "获取文件列表")
    @GetMapping("/list")
    public Result<List<FileVO>> listFiles(
            @RequestParam Long parentId,
            @RequestParam(required = false, defaultValue = "time") String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fileType
    ) {
        Long userId = UserContext.getUserId();
        return Result.success(fileService.listFiles(userId, parentId, sort, keyword, fileType));
    }

    @Operation(description = "新建文件夹")
    @PostMapping("/folder")
    public Result<Long> createFolder(
            @RequestParam Long parentId,
            @RequestParam String folderName
    ) {
        Long userId = UserContext.getUserId();
        return Result.success(fileService.createFolder(userId, parentId, folderName));
    }

    @Operation(description = "重命名文件")
    @PutMapping("/rename")
    public Result<Void> renameFile(
            @RequestParam Long fileId,
            @RequestParam String newName
    ) {
        Long userId = UserContext.getUserId();
        fileService.renameFile(userId, fileId, newName);
        return Result.success();
    }

    @Operation(description = "移动文件")
    @PutMapping("/move")
    public Result<Void> moveFile(
            @RequestParam List<Long> fileIds,
            @RequestParam Long targetParentId
    ) {
        Long userId = UserContext.getUserId();
        fileService.moveFile(userId, fileIds, targetParentId);
        return Result.success();
    }

    @Operation(description = "计算用户已使用空间")
    @GetMapping("/space")
    public Result<SpaceInfo> calculateSpace() {
        Long userId = UserContext.getUserId();
        return Result.success(fileService.calculateUsedSpace(userId));
    }

    @Operation(description = "批量上传文件")
    @PostMapping("/upload/batch")
    public Result<List<Long>> uploadBatch(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("parentId") Long parentId
    ) {
        Long userId = UserContext.getUserId();
        List<Long> fileIds = new ArrayList<>();
        for (MultipartFile file : files) {
            fileIds.add(fileService.uploadFile(file, parentId, userId));
        }
        return Result.success(fileIds);
    }

    @Operation(description = "单文件上传")
    @PostMapping("/upload")
    public Result<Long> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("parentId") Long parentId
    ) {
        Long userId = UserContext.getUserId();
        Long fileId = fileService.uploadFile(file, parentId, userId);
        return Result.success(fileId);
    }

    @Operation(description = "批量删除文件（逻辑删除，移入回收站）")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody java.util.Map<String, java.util.List<Long>> body) {
        Long userId = UserContext.getUserId();
        java.util.List<Long> fileIds = body != null ? body.get("fileIds") : null;
        fileService.deleteFiles(userId, fileIds);
        return Result.success();
    }

    @Operation(description = "转存分享文件到我的网盘")
    @PostMapping("/save-share")
    public Result<Long> saveShare(
            @RequestParam Long fileId,
            @RequestParam(required = false, defaultValue = "0") Long targetParentId
    ) {
        Long userId = UserContext.getUserId();
        // 复用moveFile逻辑实现转存
        // 注意：这里需要复制文件而不是移动，避免影响原分享者的文件
        Long newFileId = fileService.copyFile(userId, fileId, targetParentId);
        return Result.success(newFileId);
    }

    @Operation(description = "获取回收站文件列表")
    @GetMapping("/recycle/list")
    public Result<List<FileVO>> listDeletedFiles() {
        Long userId = UserContext.getUserId();
        return Result.success(fileService.listDeletedFiles(userId));
    }

    @Operation(description = "还原文件")
    @PostMapping("/recycle/restore")
    public Result<Void> restoreFiles(@RequestBody java.util.Map<String, java.util.List<Long>> body) {
        Long userId = UserContext.getUserId();
        java.util.List<Long> fileIds = body != null ? body.get("fileIds") : null;
        fileService.restoreFiles(userId, fileIds);
        return Result.success();
    }

    @Operation(description = "彻底删除文件")
    @PostMapping("/recycle/delete")
    public Result<Void> permanentlyDelete(@RequestBody java.util.Map<String, java.util.List<Long>> body) {
        Long userId = UserContext.getUserId();
        java.util.List<Long> fileIds = body != null ? body.get("fileIds") : null;
        fileService.permanentlyDeleteFiles(userId, fileIds);
        return Result.success();
    }
}