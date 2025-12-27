package com.buaa.clouddisk.module.storage;

import com.buaa.clouddisk.common.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

@Component
@Slf4j
public class StorageEngine {

    @Value("${clouddisk.upload-path}")
    private String rootPath;

    /**
     * 物理存储逻辑：/用户ID/2025/12/ (角色B 核心任务 2)
     */
    public String store(MultipartFile file, Long userId) throws IOException {
        // 1. 生成相对路径：用户ID/年/月/
        LocalDate now = LocalDate.now();
        String relativePath = String.format("%d/%d/%02d/", userId, now.getYear(), now.getMonthValue());
        
        // 2. 确保物理目录存在
        File dir = new File(rootPath, relativePath);
        if (!dir.exists()) {
            dir.mkdirs(); // 递归创建文件夹
        }

        // 3. 处理重名并获取最终物理名 (角色B 核心任务 5)
        String finalName = FileUtil.getUniqueFileName(dir, file.getOriginalFilename());

        // 4. 保存文件（再次确保父目录存在，避免偶发路径不存在问题）
        File targetFile = new File(dir, finalName);
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("无法创建目录: " + parentDir.getAbsolutePath());
            }
        }
        file.transferTo(targetFile);

        log.info("文件已存储至: {}", targetFile.getAbsolutePath());

        // 返回数据库要存的相对路径
        return relativePath + finalName;
    }
}