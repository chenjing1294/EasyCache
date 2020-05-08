package com.easycache;

import java.io.Serializable;

/**
 * 缓存的键
 *
 * @author 陈敬
 * @since 1.0.0-SNAPSHOT
 */
public class CacheKey implements Serializable {
    private String key;
    private String subKey;
    private String namespace;

    public CacheKey(String namespace, String key, String subKey) {
        this.namespace = namespace;
        this.key = key;
        this.subKey = subKey;
    }

    public CacheKey(String namespace, String key) {
        this(namespace, key, null);
    }

    public String getKey() {
        if (namespace != null) {
            return namespace + ":" + key;
        } else {
            return key;
        }
    }

    public String getSubKey() {
        return subKey;
    }

    @Override
    public int hashCode() {
        String tem = namespace + key;
        if (subKey != null) {
            tem += subKey;
        }
        return tem.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof CacheKey)) {
            return false;
        } else {
            CacheKey other = (CacheKey) obj;
            if (namespace.equals(other.namespace) && key.equals(other.key)) {
                if (subKey != null) {
                    return subKey.equals(other.subKey);
                } else {
                    return other.subKey == null;
                }
            } else {
                return false;
            }
        }
    }
}
