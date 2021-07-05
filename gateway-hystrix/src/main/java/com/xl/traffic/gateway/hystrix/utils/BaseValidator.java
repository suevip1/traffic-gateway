package com.xl.traffic.gateway.hystrix.utils;

import com.xl.traffic.gateway.core.utils.AssertUtil;

public class BaseValidator {


    /**
     * 基础参数校验
     *
     * @param appGroupName
     * @param appName
     * @return: void
     * @author: xl
     * @date: 2021/7/5
     **/
    public static void baseParamValidator(String appGroupName, String appName) {
        AssertUtil.notBlack(appGroupName, "appGroupName 不能为空 ！");
        AssertUtil.notBlack(appName, "appName 不能为空 ！");
    }


}
