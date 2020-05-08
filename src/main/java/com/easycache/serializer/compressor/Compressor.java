package com.easycache.serializer.compressor;

/**
 * 可以为每个序列化器安装一个压缩器
 *
 * @author 陈敬
 * @since 1.0.0-SNAPSHOT
 */
public interface Compressor {
    /**
     * 压缩
     *
     * @param data 要被压缩的数据
     * @return 压缩后的数据
     */
    byte[] compress(byte[] data) throws CompressorException;


    /**
     * 解压
     *
     * @param data 要被解压的数据
     * @return 解压后的数据
     */
    byte[] decompress(byte[] data) throws CompressorException;
}
