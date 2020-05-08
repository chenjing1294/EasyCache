package com.easycache.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 缓存时对键{@link com.easycache.CacheKey}和值{@link com.easycache.CacheValue}进行序列化
 *
 * @author 陈敬
 * @since 1.0.0-SNAPSHOT
 */
public interface Serializer<T> {
    byte[] serialize(final T obj) throws IOException;

    T deserialize(final byte[] bytes, final Type returnType) throws IOException;

    default Charset getCharset() {
        return StandardCharsets.UTF_8;
    }
}
