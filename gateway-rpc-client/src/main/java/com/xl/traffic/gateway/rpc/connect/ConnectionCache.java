package com.xl.traffic.gateway.rpc.connect;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.core.utils.ThreadPoolUtils;
import com.xl.traffic.gateway.rpc.client.RpcClient;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author xl
 * @date: 2020-12-18
 * @desc: 负责管理各个channel的 rpcClient的,
 * 其中channel key：ip_port_index
 */
@Slf4j
public class ConnectionCache {


    private static Cache<String, RpcClient> clientMap = Caffeine.newBuilder()
            .maximumSize(GatewayConstants.CONNECT_CACHE_MAX_SIZE)
            .executor(ThreadPoolUtils.getExecutorCaffinePool())
            .build();

    /**
     * 获取连接数量
     *
     * @return: long
     * @author: xl
     * @date: 2021/6/24
     **/
    public static long rpcPoolSize() {
        return clientMap.estimatedSize();
    }

    public static RpcClient get(String key) {
        return clientMap.getIfPresent(key);
    }

    public static void putIfAbsent(String key, RpcClient client) {

        clientMap.put(key, client);
        show();
    }

    public static void remove(String key) {
        clientMap.invalidate(key);
        show();
    }

    /**
     * 展示连接数量
     */
    public static void show() {
        log.info("####### 当前连接池数量: {}  连接池信息：{}", rpcPoolSize(), clientMap);
    }
}
