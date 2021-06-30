package com.xl.traffic.gateway.hystrix.downgrade.easy;

/**
 * 业务方法，不带有返回值的业务执行
 *
 * @author: xl
 * @date: 2021/6/29
 **/
@FunctionalInterface
public interface BizFunctionWithoutReturn {


    /**
     * 执行业务逻辑不返回结果
     *
     * @author: xl
     * @date: 2021/6/29
     **/
    void invokeBizMethod();

}
