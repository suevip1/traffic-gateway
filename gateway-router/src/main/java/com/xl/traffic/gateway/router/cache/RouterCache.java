package com.xl.traffic.gateway.router.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.xl.traffic.gateway.core.redis.LettcueRedisApi;
import com.xl.traffic.gateway.core.utils.CacheKeyUtil;
import com.xl.traffic.gateway.core.utils.GatewayConstants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 路由缓存 用户关系
 *
 * @author: xl
 * @date: 2021/7/6
 **/
public class RouterCache {

    private static class InstanceHolder {
        public static final RouterCache instance = new RouterCache();
    }

    public static RouterCache getInstance() {
        return RouterCache.InstanceHolder.instance;
    }

    /**
     * 本地缓存，数据结构:用户id-{设备id-gatewayIp}
     */
    LoadingCache<String, Map<String, String>> userLoginCacheMap =
            Caffeine.newBuilder()
                    .maximumSize(GatewayConstants.CONNECT_CACHE_MAX_SIZE)
                    .build(this::loadUserLoginInfo);


    /**
     * 添加用户登录信息本地缓存
     *
     * @param userId   用户id
     * @param deviceId 设备id
     * @param ip       gateway ip
     * @return: void
     * @author: xl
     * @date: 2021/7/6
     **/
    public void addUserLogin(String userId, String deviceId, String ip) {
        Map<String, String> map = userLoginCacheMap.get(userId);
        map.put(deviceId, ip);
    }


    /**
     * 删除用户登录信息本地缓存
     *
     * @param userId   用户id
     * @param deviceId 设备id
     * @return: void
     * @author: xl
     * @date: 2021/7/6
     **/
    public void delUserLogin(String userId, String deviceId) {

        Map<String, String> map = userLoginCacheMap.get(userId);
        map.remove(deviceId);
    }

    /**
     * 获取用户登录信息ip本地缓存根据deviceId
     *
     * @param userId   用户id
     * @param deviceId 设备id
     * @return: void
     * @author: xl
     * @date: 2021/7/6
     **/
    public String getUserLoginIpByDeviceId(String userId, String deviceId) {
        Map<String, String> map = userLoginCacheMap.get(userId);
        return map.get(deviceId);
    }

    /**
     * 获取用户登录信息本地缓存 gateway 集合
     *
     * @param userId 用户id
     * @return: void
     * @author: xl
     * @date: 2021/7/6
     **/
    public Map<String, String> getUserLoginIps(String userId) {
        return userLoginCacheMap.get(userId);
    }

    /**
     * Redis初始化该用户的连接信息
     *
     * @param userId
     * @return: java.util.Map<java.lang.String, java.lang.String>
     * @author: xl
     * @date: 2021/7/6
     **/

    private Map<String, String> loadUserLoginInfo(String userId) {
        String key = CacheKeyUtil.getUserLoginRedisCacheKey(userId);
        Map<String, String> cacheMap = LettcueRedisApi.hgetAll(key);
        if (cacheMap == null) {
            return new ConcurrentHashMap<>();
        }
        return cacheMap;
    }


}
