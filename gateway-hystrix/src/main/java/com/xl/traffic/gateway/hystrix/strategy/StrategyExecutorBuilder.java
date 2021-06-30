package com.xl.traffic.gateway.hystrix.strategy;

/**
 * 策略链构建
 *
 * @author: xl
 * @date: 2021/6/28
 **/
public interface StrategyExecutorBuilder {


    /**
     * 策略链构建
     *
     * @param
     * @return: com.xl.traffic.gateway.hystrix.strategy.AbstractStrategyExecutor
     * @author: xl
     * @date: 2021/6/28
     **/
    AbstractStrategyExecutor build();


}
