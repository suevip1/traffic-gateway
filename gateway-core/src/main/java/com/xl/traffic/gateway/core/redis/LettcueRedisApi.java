package com.xl.traffic.gateway.core.redis;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.support.ConnectionPoolSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * lettcue 高级Redis客户端
 *
 * @author: xl
 * @date: 2021/7/6
 **/
@Slf4j
public class LettcueRedisApi {

    private static GenericObjectPool<StatefulRedisConnection<String, String>> pool;

    /**
     * 构建redis连接池
     */
    public static void initPool(String address,
                                String mode,
                                String masterName,
                                String passWord) {
        log.info("redisMode:{}, redisAddress:{}, masterName:{} ", mode, address, masterName);
        try {
            getJedisBySingMachine(address, passWord);
        } catch (Exception e) {
            log.error("redis init error,message: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取redis  单机redis 环境的 redis client 进行读写
     */
    private static void getJedisBySingMachine(String address, String passWord) {
        List<String> part = Lists.newArrayList(Splitter.on(":").trimResults().split(address));
        Preconditions.checkState(part.size() == 2, "[RedisApi] redis address Must be defined as 'host:port'");
        String host = part.get(0);
        int port = Integer.parseInt(part.get(1));
        RedisURI redisUri = RedisURI.builder()                    // <1> 创建单机连接的连接信息
                .withHost(host)
                .withPort(port)
                .withTimeout(Duration.of(30000, ChronoUnit.SECONDS))
                .build();
        RedisClient redisClient = RedisClient.create(redisUri);   // <2> 创建客户端
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        //redis最大连接数
        genericObjectPoolConfig.setMaxTotal(500);
        //空闲时最大连接数
        genericObjectPoolConfig.setMaxIdle(100);
        //空闲时最小连接数
        genericObjectPoolConfig.setMinIdle(100);
        pool = ConnectionPoolSupport.createGenericObjectPool(redisClient::connect, genericObjectPoolConfig);
    }


    /**
     * 获取连接池
     */
    private static StatefulRedisConnection<String, String> getLettcueRedisResource() {
        StatefulRedisConnection<String, String> connection = null;
        try {
            connection = pool.borrowObject();     // <3> 创建线程安全的连接
        } catch (Exception ex) {
            log.error("error:{}", ex);
        }
        return connection;
    }

    /**
     * 设置Map值
     */
    public static void hset(String key, String field, String value) {
        if (Objects.isNull(key) || Objects.isNull(value) || Objects.isNull(field)) {
            return;
        }
        StatefulRedisConnection<String, String> jedis = null;
        try {
            jedis = getLettcueRedisResource();
            jedis.async().hset(key, field, value);
        } catch (Exception e) {
            log.error("occur exception when exec hset command,the message:{}", e.getMessage(), e);
        } finally {
            // 返还到连接池
            pool.returnObject(jedis);
        }
    }

    /**
     * 从Map中获取value
     */
    public static String hget(String key, String field) {
        String value = null;
        if (Objects.isNull(key) || Objects.isNull(field)) {
            return null;
        }
        StatefulRedisConnection<String, String> jedis = null;
        try {
            jedis = getLettcueRedisResource();
            value = jedis.sync().hget(key, field);
        } catch (Exception e) {
            log.error("occur exception when exec hget command,the message:{}", e.getMessage(), e);
        } finally {
            pool.returnObject(jedis);
        }
        return value;
    }

    /**
     * 从Map中获取value
     */
    public static Map<String, String> hgetAll(String key) {
        Map<String, String> value = null;
        if (Objects.isNull(key)) {
            return null;
        }
        StatefulRedisConnection<String, String> jedis = null;
        try {
            jedis = getLettcueRedisResource();
            value = jedis.sync().hgetall(key);
        } catch (Exception e) {
            log.error("occur exception when exec hget command,the message:{}", e.getMessage(), e);
        } finally {
            pool.returnObject(jedis);
        }
        return value;
    }

    /**
     * 从Map中删除value
     */
    public static Long hdel(String key, String... field) {
        Long value = 0l;
        if (Objects.isNull(key) || Objects.isNull(field)) {
            return value;
        }
        StatefulRedisConnection<String, String> jedis = null;
        try {
            jedis = getLettcueRedisResource();
            jedis.async().hdel(key, field);
        } catch (Exception e) {
            log.error("exception when exec hdel command,the message:{}", e.getMessage(), e);
        } finally {
            // 返还到连接池
            pool.returnObject(jedis);
        }
        return value;
    }




    public static void expire(String key, int seconds) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        StatefulRedisConnection<String, String> jedis = null;
        try {
            jedis = getLettcueRedisResource();
            jedis.sync().expire(key, seconds);
        } catch (Exception e) {
            log.error("exception when exec expire command,the message:{}", e.getMessage(), e);
        } finally {
            // 返还到连接池
            pool.returnObject(jedis);
        }
    }
}
