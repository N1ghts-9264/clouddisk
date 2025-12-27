package com.buaa.clouddisk.module.file.controller;

import com.buaa.clouddisk.common.result.Result;
import com.buaa.clouddisk.common.util.UserContext;
import com.buaa.clouddisk.module.file.service.IFileService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileUploadController {
    private final IFileService fileService;


    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
            @RequestParam("parentId") Long parentId) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) userId = 1L; 

            Long fileId = fileService.uploadFile(file, parentId, userId);
            Map<String, Object> data = new HashMap<>();
            data.put("file_id", fileId);
            return Result.success(data);
        } catch (RuntimeException e) {
            // 如果 Service 报错了，我们捕获它并返回 Result.error
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("上传发生意外错误");
        }
    }

    /**
     * 批量上传接口 (角色 B 任务 1 进阶)
     */
    @PostMapping("/upload/batch")
    public Result<List<Long>> uploadBatch(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("parentId") Long parentId) {
        
        Long userId = UserContext.getUserId();
        if (userId == null) userId = 1L;

        List<Long> fileIds = new ArrayList<>();
        for (MultipartFile file : files) {
            fileIds.add(fileService.uploadFile(file, parentId, userId));
        }
        
        return Result.success(fileIds);
    }
}