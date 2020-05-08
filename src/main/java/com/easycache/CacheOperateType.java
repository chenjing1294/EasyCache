package com.easycache;

/**
 * 缓存的操作类型
 *
 * @author 陈敬
 * @since 1.0.0-SNAPSHOT
 */
public enum CacheOperateType {
    READ_WRITE, //如果缓存中有数据，则使用缓存中的数据，如果缓存中没有数据，则加载数据，并写入缓存
    WRITE,      //从数据源中加载最新的数据，并写入缓存
    READ,       //只从缓存中读取，用于其它地方往缓存写，这里只读的场景
    LOAD;       //只从数据源加载数据，不读取缓存中的数据，也不写入缓存
}
