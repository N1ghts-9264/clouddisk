package com.buaa.clouddisk.common.util;

import org.springframework.util.DigestUtils;
import java.nio.charset.StandardCharsets;

/**
 * MD5 加密工具类
 */
public class Md5Util {
    // 盐值，混淆密码，防止彩虹表破解
    private static final String SALT = "CloudDisk@2025_Buaa";

    /**
     * 生成加密后的密码
     * 算法：MD5(密码 + 盐)
     */
    public static String encrypt(String password) {
        if (password == null) {
            return null;
        }
        String str = password + SALT;
        return DigestUtils.md5DigestAsHex(str.getBytes(StandardCharsets.UTF_8));
    }
}