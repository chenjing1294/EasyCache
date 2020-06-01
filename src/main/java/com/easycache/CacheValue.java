package com.easycache;

import java.io.Serializable;

/**
 * 对要缓存的数据进行包装
 *
 * @author 陈敬
 * @since 1.0.0-SNAPSHOT
 */
public class CacheValue<T> implements Serializable {
    public static int NEVER_EXPIRE = -1;

    //缓存的数据
    private T data;

    //最后从数据源里加载的时间(ms)
    private long lastLoadTime;

    //缓存时长（s），-1表示永不过期
    private int expire;

    public CacheValue(T data, int expire) {
        this.data = data;
        this.expire = expire;
    }

    public CacheValue(T data) {
        this(data, NEVER_EXPIRE);
    }

    public CacheValue() {
    }

    /**
     * 判断缓存的数据是否获取
     */
    public boolean isExpired() {
        if (expire > 0) {
            return (System.currentTimeMillis() - lastLoadTime) > expire * 1000;
        }
        return false;
    }

    public int getExpire() {
        return expire;
    }

    public void setExpire(int expire) {
        this.expire = expire;
    }

    public long getLastLoadTime() {
        return lastLoadTime;
    }

    public void setLastLoadTime(long lastLoadTime) {
        this.lastLoadTime = lastLoadTime;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CacheValue{" +
                "data=" + data +
                ", lastLoadTime=" + lastLoadTime +
                ", expire=" + expire +
                '}';
    }
}
