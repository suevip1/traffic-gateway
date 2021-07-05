package com.xl.traffic.gateway.hystrix;

import com.xl.traffic.gateway.hystrix.service.PullAndPushService;
import lombok.Getter;
import lombok.Setter;


public abstract class AbstractDowngradeClient implements DowngradeClient {


    /**
     * appGroupName ： 应用组
     */
    @Getter
    @Setter
    protected String appGroupName;

    /**
     * appName ： 应用组名称
     */
    @Getter
    @Setter
    protected String appName;


    public AbstractDowngradeClient(String appGroupName, String appName) {
        this.appGroupName = appGroupName;
        this.appName = appName;
    }


    /**
     * 每5秒从admin服务端拉取最新的降级点配置信息
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/7/5
     **/
    protected void abstractUpdatePointStrategyFromAdminServer() {
        PullAndPushService.getInstance().updatePointStrategyFromAdminServer(appGroupName, appName);
    }

    /**
     * 汇报本地的降级数据给admin
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/7/5
     **/
    protected void abstractPushDowngrateData2Admin() {
        PullAndPushService.getInstance().pushDowngrateData2Admin(appGroupName, appName);
    }


}
