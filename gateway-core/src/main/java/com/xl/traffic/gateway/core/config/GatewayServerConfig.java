package com.xl.traffic.gateway.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GatewayServerConfig {


    public final static Properties properties;
    private final static String PROPRETIES_PATH = "/application.properties";

    /**
     * redis相关配置
     */

    public static final String REDIS_IP = "redis.ip";
    public static final String REDIS_PORT = "redis.port";

    static {
        properties = new Properties();
        try {
            InputStream is = Object.class.getResourceAsStream(PROPRETIES_PATH);
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getString(String key, String def) {
        if (containsKey(key))
            return getString(key);
        else
            return def;
    }

    public static String getString(String key) {
        return properties.getProperty(key);
    }

    public static int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    public static int getInt(String key, int def) {
        if (containsKey(key))
            return getInt(key);
        else
            return def;
    }

    // 服务器配置必须存在,否则运行异常,防止BUG
    public static String getStringNotnull(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException(key + " property value is null");
        }
        return value;
    }

    public static boolean getBooleanNotnull(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException(key + " property value is null");
        }
        return Boolean.valueOf(value);
    }

    public static boolean containsKey(String key) {
        return properties.containsKey(key);
    }


}
