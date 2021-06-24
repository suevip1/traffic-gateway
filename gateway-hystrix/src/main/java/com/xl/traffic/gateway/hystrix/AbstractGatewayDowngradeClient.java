package com.xl.traffic.gateway.hystrix;

import lombok.Getter;
import lombok.Setter;

public abstract class AbstractGatewayDowngradeClient implements GatewayDowngradeClient {


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

    public AbstractGatewayDowngradeClient(String appGroupName, String appName) {
        this.appGroupName = appGroupName;
        this.appName = appName;
    }


}
