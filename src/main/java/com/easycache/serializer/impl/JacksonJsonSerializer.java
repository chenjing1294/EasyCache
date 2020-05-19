package com.easycache.serializer.impl;

import com.easycache.serializer.Serializer;
import com.easycache.serializer.compressor.Compressor;
import com.easycache.serializer.compressor.CompressorException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 将数据序列化为JSON格式，可以选择性的安装一个压缩器{@link Compressor}
 *
 * @author 陈敬
 * @since 1.0.0-SNAPSHOT
 */
public class JacksonJsonSerializer implements Serializer<Object> {
    private final ObjectMapper MAPPER;
    private final Compressor compressor;
    private int threshold = 1024;   //压缩阈值

    public JacksonJsonSerializer() {
        this.compressor = null;
        MAPPER = new ObjectMapper();
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public JacksonJsonSerializer(Compressor compressor) {
        this.compressor = compressor;
        MAPPER = new ObjectMapper();
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public JacksonJsonSerializer(Compressor compressor, int threshold) {
        this.compressor = compressor;
        this.threshold = threshold;
        MAPPER = new ObjectMapper();
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static JavaType getJavaType(Type type) {
        //判断是否是参数化类型，即泛型
        if (type instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            //获取泛型类型
            Class rowClass = (Class) ((ParameterizedType) type).getRawType();
            JavaType[] javaTypes = new JavaType[actualTypeArguments.length];
            for (int i = 0; i < actualTypeArguments.length; i++) {
                //泛型也可能带有泛型，递归获取
                javaTypes[i] = getJavaType(actualTypeArguments[i]);
            }
            return TypeFactory.defaultInstance().constructParametricType(rowClass, javaTypes);
        } else {
            //简单类型直接用该类构建JavaType
            Class cla = (Class) type;
            return TypeFactory.defaultInstance().constructParametricType(cla, new JavaType[0]);
        }
    }

    public byte[] serialize(Object obj) throws IOException {
        if (compressor != null) {
            try {
                byte[] src = MAPPER.writeValueAsBytes(obj);
                if (src.length > threshold) {
                    byte[] compress = compressor.compress(src);
                    byte[] dest = new byte[compress.length + 1];
                    System.arraycopy(compress, 0, dest, 1, compress.length);
                    dest[0] = 0x1;
                    return dest;
                } else {
                    byte[] dest = new byte[src.length + 1];
                    System.arraycopy(src, 0, dest, 1, src.length);
                    dest[0] = 0x0;
                    return dest;
                }
            } catch (CompressorException e) {
                throw new RuntimeException(e);
            }
        } else {
            return MAPPER.writeValueAsBytes(obj);
        }
    }

    public Object deserialize(byte[] bytes, Type returnType) throws IOException {
        if (null == bytes || bytes.length == 0) {
            return null;
        }
        JavaType javaType = getJavaType(returnType);
        if (compressor != null) {
            try {
                byte[] uncompress = null;
                byte[] dest = new byte[bytes.length - 1];
                if (bytes[0] == 0x1) {//压缩格式
                    System.arraycopy(bytes, 1, dest, 0, bytes.length - 1);
                    uncompress = compressor.decompress(dest);
                } else {//非压缩格式
                    System.arraycopy(bytes, 1, dest, 0, bytes.length - 1);
                    uncompress = dest;
                }
                return MAPPER.readValue(uncompress, javaType);
            } catch (CompressorException e) {
                throw new RuntimeException(e);
            }
        } else {
            return MAPPER.readValue(bytes, javaType);
        }
    }
}
