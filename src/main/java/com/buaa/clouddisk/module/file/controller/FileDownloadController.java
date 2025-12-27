package com.buaa.clouddisk.module.file.controller;

import com.buaa.clouddisk.module.file.entity.SysFile;
import com.buaa.clouddisk.module.file.mapper.FileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileDownloadController {
    private final FileMapper fileMapper;

    @Value("${clouddisk.upload-path}")
    private String rootPath;

    /** * 单文件下载 / 在线预览 * 支持图片、视频在线播放 */
    @GetMapping("/download/{fileId}")
    public void download(@PathVariable Long fileId, HttpServletResponse response) {
        SysFile file = fileMapper.selectById(fileId);
        if (file == null) {
            response.setStatus(404);
            return;
        }
        String storedPath = file.getFilePath();
        java.io.File physicalFile;
        if (storedPath == null || storedPath.trim().isEmpty()) {
            response.setStatus(404);
            return;
        }
        java.io.File rawFile = new java.io.File(storedPath);
        if (rawFile.isAbsolute()) {
            physicalFile = rawFile;
        } else {
            physicalFile = new java.io.File(rootPath, storedPath);
        }
        if (!physicalFile.exists()) {
            response.setStatus(404);
            return;
        }

        // 1. 设置 Content-Type，让浏览器知道是图片还是视频
        String mimeType = "application/octet-stream"; // 默认二进制流
        String suffix = file.getFileType() != null ? file.getFileType().toLowerCase() : "";
        if (suffix.endsWith("jpg") || suffix.endsWith("jpeg")) mimeType = "image/jpeg";
        else if (suffix.endsWith("png")) mimeType = "image/png";
        else if (suffix.endsWith("gif")) mimeType = "image/gif";
        else if (suffix.endsWith("mp4")) mimeType = "video/mp4"; // 关键：MP4 预览

        response.setContentType(mimeType);

        // 2. 如果不是预览类型，强制浏览器弹出下载框
        if (!mimeType.startsWith("image") && !mimeType.startsWith("video")) {
            try {
                String encodedFilename = URLEncoder.encode(file.getFilename(), StandardCharsets.UTF_8.toString());
                response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFilename + "\"");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        // 3. 设置文件大小 (对视频播放进度条很重要)
        response.setContentLengthLong(physicalFile.length());

        // 4. 读取文件流输出
        try (FileInputStream fis = new FileInputStream(physicalFile);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[8192]; // 8KB buffer
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
        } catch (IOException e) {
            // 客户端中断下载（如关闭视频页面）是正常现象，仅记录 Debug
            log.debug("文件传输中断: {}", e.getMessage());
        }
    }

    /** * 批量打包下载 (Zip) * 参数：fileIds=1,2,3 */
    @GetMapping("/download/batch")
    public void batchDownload(@RequestParam List<Long> fileIds, HttpServletResponse response) {
        // 1. 设置响应头为 Zip
        response.setContentType("application/zip");
        try {
            response.setHeader("Content-Disposition", "attachment; filename=\"batch_download.zip\"");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. 创建 Zip 输出流
        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (Long id : fileIds) {
                SysFile file = fileMapper.selectById(id);
                // 简单处理：跳过不存在的文件或文件夹（Role D 暂不处理文件夹递归）
                if (file == null || file.getIsFolder() != null && file.getIsFolder()) {
                    continue;
                }
                java.io.File phyFile = new java.io.File(file.getFilePath());
                if (!phyFile.exists()) continue;

                // 3. 将文件写入 Zip
                // 注意：ZipEntry 使用文件名，如果有重名文件，Windows解压会提示覆盖，
                // 进阶做法是在这里处理重名逻辑 (如 1.jpg, 1(1).jpg)，此处简化处理。
                zos.putNextEntry(new ZipEntry(file.getFilename()));
                try (FileInputStream fis = new FileInputStream(phyFile)) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = fis.read(buffer)) != -1) {
                        zos.write(buffer, 0, len);
                    }
                }
                zos.closeEntry();
            }
            zos.flush(); // try-with-resources 会自动关闭
        } catch (IOException e) {
            log.error("打包下载失败", e);
        }
    }
}