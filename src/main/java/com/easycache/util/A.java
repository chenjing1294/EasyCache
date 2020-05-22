package com.easycache.util;

import com.easycache.CacheKey;

public class A {
    public static boolean isBatchDeleteKey(CacheKey cacheKey) {
        String k = cacheKey.getKey();
        String hk = cacheKey.getSubKey();
        if (k != null && containWildcard(k)) {
            return true;
        } else {
            return hk != null && containWildcard(hk);
        }
    }

    private static boolean containWildcard(String s) {
        return (s.contains("?") || s.contains("*") || (s.contains("[") && s.contains("]")));
    }

    /**
     * 把通配符转换为正则表达式
     *
     * @param s 包含通配符的字符串
     */
    public static String getRegex(String s) {
        return s.replaceAll("\\*", ".*").replaceAll("\\?", ".");
    }
}
