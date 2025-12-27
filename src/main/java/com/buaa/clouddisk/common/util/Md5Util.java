package com.buaa.clouddisk.common.util;

import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * MD5 工具类
 */
public class Md5Util {

    // 密码盐值
    private static final String SALT = "CloudDisk@2025_Buaa";

    /**
     * 密码加密：MD5(password + salt)
     */
    public static String encrypt(String password) {
        if (password == null) {
            return null;
        }
        String str = password + SALT;
        return DigestUtils.md5DigestAsHex(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算文件 MD5（用于秒传/去重）
     */
    public static String getFileMd5(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            return DigestUtils.md5DigestAsHex(is);
        } catch (IOException e) {
            throw new RuntimeException("计算文件 MD5 失败", e);
        }
    }
}
