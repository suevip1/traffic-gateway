package com.xl.traffic.gateway.hystrix.strategy;

import com.xl.traffic.gateway.hystrix.strategy.executor.*;

/**
 * 策略链构建默认实现类
 *
 * @author: xl
 * @date: 2021/6/28
 **/
public class DefaultStrategyExecutorBuilder implements StrategyExecutorBuilder {

    @Override
    public AbstractStrategyExecutor build() {

        /**默认执行策略顺序：熔断>秒访问量>token令牌桶>并发访问>超时时间访问>异常次数访问>异常率访问*/
        return
                new FuseStrategyExecutor(
                        new VisitStrategyExecutor(
                                new TokenBucketStrategyExecutor(
                                        new ConcurrentStrategyExecutor(
                                                new TimeoutStrategyExecutor(
                                                        new ExceptionStrategyExecutor(
                                                                new ExceptionRateStrategyExecutor(null)))))));
    }
}
