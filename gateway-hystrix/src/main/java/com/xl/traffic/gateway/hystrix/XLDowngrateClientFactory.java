package com.xl.traffic.gateway.hystrix;

import com.xl.traffic.gateway.core.utils.AssertUtil;
import com.xl.traffic.gateway.hystrix.downgrade.CommondDowngradeClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static com.xl.traffic.gateway.hystrix.constant.DowngradeConstant.APP_GROUP_NAME;
import static com.xl.traffic.gateway.hystrix.constant.DowngradeConstant.APP_NAME;

/**
 * XLDowngrateClientFactory 单例工厂
 *
 * @author: xl
 * @date: 2021/6/28
 **/
@Slf4j
public class XLDowngrateClientFactory {


    /**
     * 单例
     */
    private volatile static AbstractDowngradeClient instance = null;


    static {

        String appGroupName = System.getProperty(APP_GROUP_NAME);
        String appName = System.getProperty(APP_NAME);

        if (StringUtils.isBlank(appGroupName) || StringUtils.isBlank(appName)) {
            log.info("XLDowngrateClientFactory#static 系统参数" + APP_GROUP_NAME + ", " + APP_NAME +
                    "没配置全，不通过系统参数初始化 AbstractGatewayDowngradeClient。");
        } else {
            instance = buildClient(appGroupName, appName);
        }
    }


    /**
     * 获取或创建一个SdsClient实例
     *
     * @param appGroupName   应用组名称
     * @param appName        应用名称
     * @param serverAddrList Sds服务端地址列表，可以填写多个服务端地址，为了HA和Load Balance，多个地址用英文逗号分隔
     * @return
     */
    public static AbstractDowngradeClient getOrCreateSdsClient(String appGroupName, String appName, String serverAddrList) {

        if (instance != null) {
            log.warn("SdsClientFactory#getOrCreateSdsClient 创建SdsClient实例失败，因为单例SdsClient已经存在，" +
                    "appGroupName：" + instance.getAppGroupName() + ", appName:" + instance.getAppName());

            return instance;
        }
        synchronized (XLDowngrateClientFactory.class) {
            if (instance != null) {
                log.warn("SdsClientFactory#getOrCreateSdsClient 创建SdsClient实例失败，因为单例SdsClient已经存在，" +
                        "appGroupName：" + instance.getAppGroupName() + ", appName:" + instance.getAppName());
                return instance;
            }
            instance = buildClient(appGroupName, appName);
        }
        return instance;
    }

    /**
     * 获取当前的SdsClient实例
     *
     * @return
     */
    public static AbstractDowngradeClient getDowngrateClient() {
        return instance;
    }


    private static AbstractDowngradeClient buildClient(String appGroupName, String appName) {
        AssertUtil.notBlack(appGroupName, "appGroupName can not black!");
        AssertUtil.notBlack(appName, "appName can not black!");
//        AssertUtil.notBlack(serverAddrList, "serverAddrList can not black!");

        // 默认返回CommonSdsClient
        AbstractDowngradeClient sdsClient = new CommondDowngradeClient(appGroupName, appName);
        log.info("XLDowngrateClientFactory#buildSdsClient 创建SdsClient成功，sdsClient：" + sdsClient);

        return sdsClient;
    }


}
