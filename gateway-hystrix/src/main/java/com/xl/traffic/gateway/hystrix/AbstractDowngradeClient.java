package com.xl.traffic.gateway.hystrix;

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


}
