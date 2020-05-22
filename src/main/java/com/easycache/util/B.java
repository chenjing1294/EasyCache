package com.easycache.util;

import com.easycache.CacheKey;
import com.easycache.CacheValue;
import com.easycache.EasyCacheConfig;
import com.easycache.manager.CacheManager;
import com.easycache.manager.factory.CacheManagerFactory;
import com.easycache.manager.factory.JedisCacheManagerFactory;
import com.easycache.serializer.compressor.impl.CommonCompressor;
import com.easycache.serializer.impl.JacksonJsonSerializer;
import com.easycache.serializer.impl.StringSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Scanner;

public class B {
    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 6379;
        String password = null;
        if (args.length > 0 && args[0] != null) {
            host = args[0];
        }

        if (args.length > 1 && args[1] != null) {
            port = Integer.valueOf(args[1]);
        }

        if (args.length > 2 && args[2] != null) {
            password = args[2];
        }
        EasyCacheConfig easyCacheConfig = new EasyCacheConfig();
        JedisPool jedisPool = new JedisPool(new GenericObjectPoolConfig(), host, port, Protocol.DEFAULT_TIMEOUT, password);
        CacheManagerFactory f1 = new JedisCacheManagerFactory(
                jedisPool,
                new StringSerializer(),
                new JacksonJsonSerializer(new CommonCompressor()),
                easyCacheConfig,
                null);
        CacheManager cacheManager = f1.create();
        System.out.println("Successfully connected to the redis server.");

        Scanner scanner = new Scanner(System.in);
        System.out.println("You can only use the following two commands:");
        System.out.println("hget <key> <field>");
        System.out.println("get <key>");
        System.out.println("exit");
        ParameterizedTypeImpl make = ParameterizedTypeImpl.make(CacheValue.class, new Type[]{Object.class}, null);
        ObjectMapper mapper = new ObjectMapper();
        while (true) {
            System.out.print(host + ":" + port + ">");
            String command = scanner.nextLine();
            if (command.startsWith("hget")) {
                String[] split = command.split(" ");
                CacheKey cacheKey = new CacheKey(null, split[1], split[2]);
                CacheValue<Object> value = cacheManager.get(cacheKey, make);
                System.out.println(toPrettyFormat(mapper.writeValueAsString(value)));
            } else if (command.startsWith("get")) {
                String[] split = command.split(" ");
                CacheKey cacheKey = new CacheKey(null, split[1]);
                CacheValue<Object> value = cacheManager.get(cacheKey, make);
                System.out.println(toPrettyFormat(mapper.writeValueAsString(value)));
            } else if (command.equals("exit")) {
                break;
            }
        }
    }

    private static String toPrettyFormat(String json) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jsonObject);
    }
}
