package com.xl.traffic.gateway.hystrix.strategy;

import com.xl.traffic.gateway.hystrix.strategy.executor.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 策略链构建默认实现类
 *
 * @author: xl
 * @date: 2021/6/28
 **/
public class DefaultStrategyExecutorBuilder {


    private static class InstanceHolder {
        public static final DefaultStrategyExecutorBuilder instance = new DefaultStrategyExecutorBuilder();
    }

    public static DefaultStrategyExecutorBuilder getInstance() {
        return InstanceHolder.instance;
    }

    @Getter
    @Setter
    private AbstractStrategyExecutor abstractStrategyExecutor;

    public DefaultStrategyExecutorBuilder() {
        this.abstractStrategyExecutor = build();
    }

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
