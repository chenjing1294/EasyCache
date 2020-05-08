package com.easycache.serializer.compressor;

/**
 * 对数据进行压缩时产生的异常
 *
 * @author 陈敬
 * @since 1.0.0-SNAPSHOT
 */
public class CompressorException extends Exception {
    public CompressorException(String message) {
        super(message);
    }

    public CompressorException(String message, Throwable cause) {
        super(message, cause);
    }
}
