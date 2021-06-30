package com.xl.traffic.gateway.hystrix.downgrade.easy;


/**
 * 降级后执行的方法，相当于fallback ，没有返回值
 *
 * @author: xl
 * @date: 2021/6/29
 **/
@FunctionalInterface
public interface DowngrateFunctionWithoutReturn {


    /**
     * 执行降级后执行的方法
     *
     * @param
     * @author: xl
     * @date: 2021/6/29
     **/
    void invokeDowngrateMethod();


}
