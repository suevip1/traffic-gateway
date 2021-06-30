package com.xl.traffic.gateway.hystrix.downgrade.easy;

/**
 * 业务方法，带有返回值的业务执行
 *
 * @author: xl
 * @date: 2021/6/29
 **/
@FunctionalInterface
public interface BizFunction<T> {


    /**
     * 执行业务逻辑并返回结果
     *
     * @return: T
     * @author: xl
     * @date: 2021/6/29
     **/
    T invokeBizMethod();

}
