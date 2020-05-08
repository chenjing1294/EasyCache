package com.easycache.serializer.impl;

import com.easycache.serializer.Serializer;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringSerializer implements Serializer<String> {
    private final Charset charset;

    public StringSerializer() {
        charset = StandardCharsets.UTF_8;
    }

    public StringSerializer(Charset charset) {
        this.charset = charset;
    }

    public byte[] serialize(String obj) {
        return obj.getBytes(charset);
    }

    public String deserialize(byte[] bytes, Type returnType) {
        return (bytes == null ? null : new String(bytes, charset));
    }

    public Charset getCharset() {
        return charset;
    }
}
