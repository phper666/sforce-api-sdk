package com.phper666.sforce.api.sdk.internal;


/**
 * @author Yuzhao.LI
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
public class FileUtils {
    /**
     * 返回文件拓展
     * "c:/t/bbb" 返回 ""
     * "/t/bbb.jpg" 返回 "jpg"
     *
     * @param fileName
     * @return
     */
    public static String fileExtName(String fileName) {
        if (fileName == null) {
            return null;
        }
        final int index = fileName.lastIndexOf(".");
        if (index == -1) {
            return "";
        } else {
            // 扩展名中不能包含路径相关的符号
            return fileName.substring(index + 1);
        }
    }

    /**
     * 返回文件名称
     * "c:/t/bbb" 返回 "bbb"
     * "/t/bbb.jpg" 返回 "bbb.jpg"
     *
     * @param filePath
     * @return
     */
    public static String getFileName(String filePath) {
        if (null == filePath) {
            return null;
        }
        int len = filePath.length();
        if (0 == len) {
            return filePath;
        }
        if (FileUtils.isFileSeparator(filePath.charAt(len - 1))) {
            // 以分隔符结尾的去掉结尾分隔符
            len--;
        }

        int begin = 0;
        char c;
        for (int i = len - 1; i > -1; i--) {
            c = filePath.charAt(i);
            if (FileUtils.isFileSeparator(c)) {
                // 查找最后一个路径分隔符（/或者\）
                begin = i + 1;
                break;
            }
        }

        return filePath.substring(begin, len);
    }

    /**
     * 返回文件名称，不带后缀
     * "c:/t/bbb" 返回 "bbb"
     * "/t/bbb.jpg" 返回 "bbb"
     *
     * @param filePath
     * @return
     */
    public static String fileMainName(String filePath) {
        if (null == filePath) {
            return null;
        }
        int len = filePath.length();
        if (0 == len) {
            return filePath;
        }

        if (isFileSeparator(filePath.charAt(len - 1))) {
            len--;
        }

        int begin = 0;
        int end = len;
        char c;
        for (int i = len - 1; i >= 0; i--) {
            c = filePath.charAt(i);
            if (len == end && '.' == c) {
                // 查找最后一个文件名和扩展名的分隔符：.
                end = i;
            }
            // 查找最后一个路径分隔符（/或者\），如果这个分隔符在.之后，则继续查找，否则结束
            if (isFileSeparator(c)) {
                begin = i + 1;
                break;
            }
        }

        return filePath.substring(begin, end);
    }

    public static boolean isFileSeparator(char c) {
        return '/' == c || '\\' == c;
    }
}
