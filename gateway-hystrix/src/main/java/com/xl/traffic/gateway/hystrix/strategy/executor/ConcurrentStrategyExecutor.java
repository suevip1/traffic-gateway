package com.xl.traffic.gateway.hystrix.strategy.executor;

import com.xl.traffic.gateway.hystrix.enums.DowngradeActionType;
import com.xl.traffic.gateway.hystrix.model.CheckData;
import com.xl.traffic.gateway.hystrix.model.Strategy;
import com.xl.traffic.gateway.hystrix.strategy.AbstractStrategyExecutor;

/**
 * 并发降级策略
 *
 * @author: xl
 * @date: 2021/6/24
 **/
public class ConcurrentStrategyExecutor extends AbstractStrategyExecutor {

    public ConcurrentStrategyExecutor(AbstractStrategyExecutor next) {
        super(next);
    }

    @Override
    protected boolean strategyCheck(CheckData checkData, Strategy strategy) {

        if (checkData.getConcurrentAcquire() == null) {
            return true;
        }

        /**由于并发策略使用啦信号量，所以在统计的同时已经起到啦策略判断的工鞥，所以这里直接返回判断结果*/
        return checkData.getConcurrentAcquire();
    }

    @Override
    protected DowngradeActionType getStrategyType() {
        return DowngradeActionType.CONCURRENT;
    }
}
