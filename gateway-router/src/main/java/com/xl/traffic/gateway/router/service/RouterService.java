package com.xl.traffic.gateway.router.service;

import com.xl.traffic.gateway.core.redis.LettcueRedisApi;
import com.xl.traffic.gateway.core.utils.CacheKeyUtil;
import com.xl.traffic.gateway.router.cache.RouterCache;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * router 用户路由关系处理
 *
 * @author: xl
 * @date: 2021/7/6
 **/
@Slf4j
public class RouterService {

    private static class InstanceHolder {
        public static final RouterService instance = new RouterService();
    }

    public static RouterService getInstance() {
        return RouterService.InstanceHolder.instance;
    }


    /**
     * 用户登录，保存登录gateway信息
     *
     * @param userId   用户id
     * @param deviceId 设备id
     * @param ip       gateway ip
     * @return: boolean
     * @author: xl
     * @date: 2021/7/6
     **/
    public boolean userLogin(String userId, String deviceId, String ip) {
        try {
            RouterCache.getInstance().addUserLogin(userId, deviceId, ip);
            LettcueRedisApi.hset(CacheKeyUtil.getUserLoginRedisCacheKey(userId), deviceId, ip);
            return true;
        } catch (Exception ex) {
            log.error("RouterService#userLogin happend error:{}", ex);
        }
        return false;
    }


    /**
     * 获取用户登录列表
     *
     * @param userId 用户id
     * @return: java.util.Map<java.lang.String, java.lang.String>
     * @author: xl
     * @date: 2021/7/6
     **/

    public Map<String, String> getUserIpList(String userId) {
        return RouterCache.getInstance().getUserLoginIps(userId);
    }


    /**
     * 根据用户登录设备id 获取用户登录所在的gateway serverIp
     *
     * @param userId   用户id
     * @param deviceId 设备id
     * @return: java.lang.String
     * @author: xl
     * @date: 2021/7/6
     **/
    public String getUserIdByDeviceId(String userId, String deviceId) {
        return RouterCache.getInstance().getUserLoginIpByDeviceId(userId, deviceId);
    }


    /**
     * 退出登录
     *
     * @param userId   用户id
     * @param deviceId 设备id
     * @return: boolean
     * @author: xl
     * @date: 2021/7/6
     **/
    public boolean exitLogin(String userId, String deviceId) {
        try {
            RouterCache.getInstance().delUserLogin(userId, deviceId);
            LettcueRedisApi.hdel(CacheKeyUtil.getUserLoginRedisCacheKey(userId), deviceId);
            return true;
        } catch (Exception ex) {
            log.error("RouterService#userLogin happend error:{}", ex);
        }
        return false;
    }


}
