package com.xl.traffic.gateway.hystrix.utils;

import com.xl.traffic.gateway.core.utils.AssertUtil;

/**
 * 缓存key 工具类
 *
 * @author: xl
 * @date: 2021/7/5
 **/
public class CacheKeyUtil {


    /**
     * 拼接appGroupName+appName组合缓存key
     *
     * @param appGroupName 应用组
     * @param appName      应用
     * @return: java.lang.String
     * @author: xl
     * @date: 2021/7/5
     **/
    public static String getAppGroupWithAppName(String appGroupName, String appName) {
        AssertUtil.notBlack(appGroupName, "应用组不能为空！");
        AssertUtil.notBlack(appName, "应用不能为空！");
        return appGroupName + appName;
    }


    /**
     * 拼接appGroupName+appName+point组合缓存key
     *
     * @param appGroupName 应用组
     * @param appName      应用
     * @param point        降级点
     * @return: java.lang.String
     * @author: xl
     * @date: 2021/7/5
     **/
    public static String getAppGroupWithAppNameWithPoint(String appGroupName, String appName, String point) {
        AssertUtil.notBlack(appGroupName, "应用组不能为空！");
        AssertUtil.notBlack(appName, "应用不能为空！");
        AssertUtil.notBlack(point, "降级点不能为空！");
        return appGroupName + appName + point;
    }


}
