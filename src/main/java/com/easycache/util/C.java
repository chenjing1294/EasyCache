package com.easycache.util;

import org.springframework.util.StringUtils;

public class C {
    public static String getValue(String s) {
        if (StringUtils.isEmpty(s)) {
            throw new RuntimeException("参数不允许为空");
        }
        if (s.startsWith("${") && s.endsWith("}")) {
            String value = null;
            if (s.contains(":")) {
                int index = s.indexOf(":");
                value = System.getenv(s.substring(2, index));
                if (value == null) {
                    value = s.substring(index + 1, s.length() - 1);
                }
            }else {
                value = System.getenv(s.substring(2, s.length() - 1));
            }
            return value;
        } else {
            return s;
        }
    }
}
