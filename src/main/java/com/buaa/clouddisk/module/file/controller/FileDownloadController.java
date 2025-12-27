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
    public void download(@PathVariable Long fileId, 
                        @RequestHeader(value = "Range", required = false) String rangeHeader,
                        HttpServletResponse response) {
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
        else if (suffix.endsWith("bmp")) mimeType = "image/bmp";
        else if (suffix.endsWith("webp")) mimeType = "image/webp";
        else if (suffix.endsWith("svg")) mimeType = "image/svg+xml";
        else if (suffix.endsWith("mp4")) mimeType = "video/mp4";
        else if (suffix.endsWith("webm")) mimeType = "video/webm";
        else if (suffix.endsWith("ogg")) mimeType = "video/ogg";
        else if (suffix.endsWith("avi")) mimeType = "video/x-msvideo";
        else if (suffix.endsWith("mov")) mimeType = "video/quicktime";
        else if (suffix.endsWith("wmv")) mimeType = "video/x-ms-wmv";
        else if (suffix.endsWith("flv")) mimeType = "video/x-flv";
        else if (suffix.endsWith("mkv")) mimeType = "video/x-matroska";

        response.setContentType(mimeType);

        // 2. 如果不是预览类型，强制浏览器弹出下载框
        if (!mimeType.startsWith("image") && !mimeType.startsWith("video")) {
            try {
                String encodedFilename = URLEncoder.encode(file.getFilename(), StandardCharsets.UTF_8.toString());
                response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFilename + "\"");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            // 预览模式，设置 inline
            try {
                String encodedFilename = URLEncoder.encode(file.getFilename(), StandardCharsets.UTF_8.toString());
                response.setHeader("Content-Disposition", "inline; filename=\"" + encodedFilename + "\"");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        // 3. 关键：支持 Range 请求（视频进度条拖动必须）
        long fileLength = physicalFile.length();
        long start = 0;
        long end = fileLength - 1;
        
        // 视频文件必须支持 Range
        if (mimeType.startsWith("video")) {
            response.setHeader("Accept-Ranges", "bytes");
            
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                // 解析 Range 请求：bytes=start-end
                String range = rangeHeader.substring(6);
                int dashIndex = range.indexOf('-');
                try {
                    if (dashIndex > 0) {
                        start = Long.parseLong(range.substring(0, dashIndex));
                        if (dashIndex < range.length() - 1) {
                            end = Long.parseLong(range.substring(dashIndex + 1));
                        }
                    } else if (range.startsWith("-")) {
                        // 后 N 字节
                        start = fileLength - Long.parseLong(range.substring(1));
                    }
                } catch (NumberFormatException e) {
                    // 忽略错误的 Range
                }
                
                // 设置 206 Partial Content
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
                response.setContentLengthLong(end - start + 1);
            } else {
                // 没有 Range 请求，返回完整文件
                response.setContentLengthLong(fileLength);
            }
        } else {
            // 非视频文件，直接返回完整文件
            response.setContentLengthLong(fileLength);
        }

        // 4. 读取文件流输出
        try (RandomAccessFile randomFile = new RandomAccessFile(physicalFile, "r");
             OutputStream os = response.getOutputStream()) {
            
            randomFile.seek(start);
            byte[] buffer = new byte[8192]; // 8KB buffer
            long bytesToRead = end - start + 1;
            
            while (bytesToRead > 0) {
                int len = randomFile.read(buffer, 0, (int) Math.min(buffer.length, bytesToRead));
                if (len == -1) break;
                os.write(buffer, 0, len);
                bytesToRead -= len;
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