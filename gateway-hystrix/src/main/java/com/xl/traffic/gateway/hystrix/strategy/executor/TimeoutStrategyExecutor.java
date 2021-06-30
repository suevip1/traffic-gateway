package com.xl.traffic.gateway.hystrix.strategy.executor;

import com.xl.traffic.gateway.hystrix.enums.DowngradeActionType;
import com.xl.traffic.gateway.hystrix.model.CheckData;
import com.xl.traffic.gateway.hystrix.model.Strategy;
import com.xl.traffic.gateway.hystrix.strategy.AbstractStrategyExecutor;
import org.springframework.core.annotation.Order;

/**
 * 超时次数降级策略
 *
 * @author: xl
 * @date: 2021/6/24
 **/
@Order(4)
public class TimeoutStrategyExecutor extends AbstractStrategyExecutor {

    public TimeoutStrategyExecutor(AbstractStrategyExecutor next) {
        super(next);
    }

    @Override
    protected boolean strategyCheck(CheckData checkData, Strategy strategy) {

        if (strategy.getTimeoutCountThreshold() < 0) {
            /**表示未配置此降级策略，故 不走该策略*/
            return true;
        }

        /**当前周期内的超时调用次数小于 配置阈值时，进行放行*/
        return checkData.getTimeoutCount() < strategy.getTimeoutCountThreshold();
    }

    @Override
    protected DowngradeActionType getStrategyType() {
        return DowngradeActionType.TIMEOUT;
    }
}
