package com.xl.traffic.gateway.core.helper;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * cmd配置
 *
 * @author: xl
 * @date: 2021/6/30
 **/
public class AppHelper {


    /**
     * 存储cmdByte-cmdValue
     */
    @Getter
    @Setter
    private static ConcurrentHashMap<Byte, String> cmdConfigMap = new ConcurrentHashMap<>();


    /**
     * 存储appGroupByte-appGroupValue
     */
    @Getter
    @Setter
    private static ConcurrentHashMap<Byte, String> appGroupConfigMap = new ConcurrentHashMap<>();

    /**
     * 存储appNameByte-appNameValue关系
     */
    @Getter
    @Setter
    private static ConcurrentHashMap<Byte, String> appNameConfigMap = new ConcurrentHashMap<>();


    private static class InstanceHolder {
        public static final AppHelper instance = new AppHelper();
    }

    public static AppHelper getInstance() {
        return AppHelper.InstanceHolder.instance;
    }


    /**
     * 从admin server 初始化cmd 配置
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/6/30
     **/
    public void loadCmdConfig() {


    }

    public String cmdValue(byte cmd) {
        return cmdConfigMap.get(cmd);
    }


    public String appGroupValue(byte appGroup) {
        return appGroupConfigMap.get(appGroup);
    }



    public String appNameValue(byte appName) {
        return appNameConfigMap.get(appName);
    }


}
