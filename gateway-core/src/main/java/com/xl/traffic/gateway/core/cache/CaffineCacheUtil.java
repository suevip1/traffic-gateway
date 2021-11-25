package com.xl.traffic.gateway.core.cache;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xl.traffic.gateway.core.metrics.DDOSMetrics;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import io.netty.channel.Channel;
import lombok.Getter;


/**
 * 高速本地缓存
 *
 * @author: xl
 * @date: 2021/7/12
 **/
public class CaffineCacheUtil {
    /**
     * DDOS本地缓存，数据结构:来源ip-滑动统计值
     */
    @Getter
    static Cache<String, DDOSMetrics> ddosMetricsCache =
            Caffeine.newBuilder()
                    .maximumSize(GatewayConstants.CONNECT_CACHE_MAX_SIZE)
                    .build();

    /**
     * ip黑名单缓存
     */
    @Getter
    static Cache<String, String> blackIpCacheMap = Caffeine.newBuilder()
            .maximumSize(GatewayConstants.CONNECT_CACHE_MAX_SIZE)
            .build();


    /**
     * 用户连接缓存
     * key：deviceId, value：channel
     */
    @Getter
    static Cache<String, Channel> connectionCache = Caffeine.newBuilder()
            .maximumSize(GatewayConstants.CONNECT_CACHE_MAX_SIZE)
            .build();


}
