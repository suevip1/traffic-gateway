package com.xl.traffic.gateway.hystrix;

import com.xl.traffic.gateway.core.utils.AssertUtil;
import com.xl.traffic.gateway.hystrix.downgrade.CommondDowngradeClient;
import lombok.extern.slf4j.Slf4j;

/**
 * XLDowngrateClientFactory 单例工厂
 *
 * @author: xl
 * @date: 2021/6/28
 **/
@Slf4j
public class XLDowngrateClientFactory {


    /**
     * 获取或创建一个SdsClient实例
     *
     * @param appGroupName   应用组名称
     * @param appName        应用名称
     * @return
     */
    public static AbstractDowngradeClient getOrCreateSdsClient(String appGroupName, String appName) {

        AssertUtil.notBlack(appGroupName, "appGroupName 不能为空 ！");
        AssertUtil.notBlack(appName, "appName  不能为空 ！");
        AbstractDowngradeClient instance = null;
        synchronized (XLDowngrateClientFactory.class) {
            instance = buildClient(appGroupName, appName);
        }
        return instance;
    }


    private static AbstractDowngradeClient buildClient(String appGroupName, String appName) {
        AssertUtil.notBlack(appGroupName, "appGroupName can not black!");
        AssertUtil.notBlack(appName, "appName can not black!");
        // 默认返回CommonSdsClient
        AbstractDowngradeClient sdsClient = new CommondDowngradeClient(appGroupName, appName);
        log.info("XLDowngrateClientFactory#buildSdsClient 创建SdsClient成功，sdsClient：" + sdsClient);
        return sdsClient;
    }


}
