package com.easycache.serializer.compressor.impl;

import com.easycache.serializer.compressor.Compressor;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CommonCompressor implements Compressor {
    private final static int BUFFER_SIZE = 1024;
    private final String compressorFormat;
    private final CompressorStreamFactory factory;

    public CommonCompressor() {
        this(CompressorStreamFactory.GZIP);
    }

    public CommonCompressor(String compressorFormat) {
        this.compressorFormat = compressorFormat;
        this.factory = new CompressorStreamFactory();
    }


    public byte[] compress(byte[] data) throws com.easycache.serializer.compressor.CompressorException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            CompressorOutputStream cos = factory.createCompressorOutputStream(compressorFormat, out);
            cos.write(data);
            cos.flush();
            cos.close();
            byte[] bytes = out.toByteArray();
            out.close();
            return bytes;
        } catch (CompressorException | IOException e) {
            throw new com.easycache.serializer.compressor.CompressorException(e.getMessage(), e);
        }
    }

    public byte[] decompress(byte[] data) throws com.easycache.serializer.compressor.CompressorException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            CompressorInputStream cis = factory.createCompressorInputStream(compressorFormat, in);
            int len;
            byte[] buf = new byte[BUFFER_SIZE];
            while ((len = cis.read(buf, 0, BUFFER_SIZE)) != -1) {
                out.write(buf, 0, len);
            }
            cis.close();
            byte[] bytes = out.toByteArray();
            out.close();
            in.close();
            return bytes;
        } catch (CompressorException | IOException e) {
            throw new com.easycache.serializer.compressor.CompressorException(e.getMessage(), e);
        }
    }
}
