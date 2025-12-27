package com.buaa.clouddisk.common.util;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 文件相关工具类
 * 功能：
 * 1. 文件后缀解析
 * 2. 危险文件类型校验
 * 3. 文件重名自动处理
 */
public class FileUtil {

    /**
     * 危险文件后缀黑名单
     */
    private static final Set<String> DANGEROUS_SUFFIX = new HashSet<>(
            Arrays.asList(".exe", ".sh", ".bat", ".cmd", ".jsp", ".php", ".js")
    );

    /**
     * 获取文件后缀（包含点，如 .txt）
     */
    public static String getSuffix(String filename) {
        if (filename == null) {
            return "";
        }
        int index = filename.lastIndexOf(".");
        if (index == -1 || index == filename.length() - 1) {
            return "";
        }
        return filename.substring(index).toLowerCase();
    }

    /**
     * 是否为危险文件
     * true：危险文件，应拒绝上传
     */
    public static boolean isDangerousFile(String filename) {
        String suffix = getSuffix(filename);
        return DANGEROUS_SUFFIX.contains(suffix);
    }

    /**
     * 获取不重名的文件名
     * 示例：test.txt -> test(1).txt
     */
    public static String getUniqueFileName(File dir, String originalName) {
        if (dir == null || originalName == null) {
            throw new IllegalArgumentException("目录或文件名不能为空");
        }

        File file = new File(dir, originalName);
        if (!file.exists()) {
            return originalName;
        }

        String name = originalName;
        String suffix = "";
        int dotIndex = originalName.lastIndexOf(".");
        if (dotIndex != -1) {
            name = originalName.substring(0, dotIndex);
            suffix = originalName.substring(dotIndex);
        }

        int count = 1;
        while (file.exists()) {
            String newName = name + "(" + count + ")" + suffix;
            file = new File(dir, newName);
            count++;
        }
        return file.getName();
    }
}
