package com.xl.traffic.gateway.hystrix.downgrade.easy;


/**
 * 降级后执行的方法，相当于fallback ，并带有返回值
 *
 * @author: xl
 * @date: 2021/6/29
 **/
@FunctionalInterface
public interface DowngrateFunction<T> {


    /**
     * 执行降级后执行的方法，并返回结果
     *
     * @param
     * @return: T
     * @author: xl
     * @date: 2021/6/29
     **/
    T invokeDowngrateMethod();


}
