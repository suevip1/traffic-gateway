package com.xl.traffic.gateway.hystrix.strategy.executor;

import com.xl.traffic.gateway.hystrix.enums.DowngradeStrategyType;
import com.xl.traffic.gateway.hystrix.model.CheckData;
import com.xl.traffic.gateway.hystrix.model.Strategy;
import com.xl.traffic.gateway.hystrix.strategy.AbstractStrategyExecutor;
import org.springframework.core.annotation.Order;

/**
 * 并发降级策略
 *
 * @author: xl
 * @date: 2021/6/24
 **/
@Order(3)
public class ConcurrentStrategyExecutor extends AbstractStrategyExecutor {

    public ConcurrentStrategyExecutor(AbstractStrategyExecutor next) {
        super(next);
    }

    @Override
    protected boolean strategyCheck(CheckData checkData, Strategy strategy) {

        if (checkData.getConcurrentAcquire() == null) {
            /**表示未配置此降级策略，故 不走该策略*/
            return true;
        }

        /**由于并发策略使用啦信号量，所以在统计的同时已经起到啦策略判断的工鞥，所以这里直接返回判断结果*/
        return checkData.getConcurrentAcquire();
    }

    @Override
    protected DowngradeStrategyType getStrategyType() {
        return DowngradeStrategyType.CONCURRENT;
    }
}
