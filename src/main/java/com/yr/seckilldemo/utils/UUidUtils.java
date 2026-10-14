package com.yr.seckilldemo.utils;

import java.util.UUID;

public class UUidUtils {

    /**
     * 生成不带横杠的 UUID
     * 例：ce21b747de5af71ab5c2e20ff0a60eea
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成带横杠的 UUID
     * 例：ce21b747-de5a-f71a-b5c2-e20ff0a60eea
     */
    public static String uuidWithDash() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成指定长度的 UUID 片段（常用于随机盐、token）
     * @param length 长度
     */
    public static String uuid(int length) {
        String uuid = uuid();
        if (length <= 0 || length > uuid.length()) {
            throw new IllegalArgumentException("长度必须在 1 到 32 之间");
        }
        return uuid.substring(0, length);
    }
}