package com.xl.traffic.gateway.hystrix.strategy.executor;

import com.xl.traffic.gateway.hystrix.enums.DowngradeActionType;
import com.xl.traffic.gateway.hystrix.model.CheckData;
import com.xl.traffic.gateway.hystrix.model.Strategy;
import com.xl.traffic.gateway.hystrix.strategy.AbstractStrategyExecutor;
import org.springframework.core.annotation.Order;

/**
 * 秒访问量次数降级策略
 *
 * @author: xl
 * @date: 2021/6/24
 **/
@Order(1)
public class VisitStrategyExecutor extends AbstractStrategyExecutor {

    public VisitStrategyExecutor(AbstractStrategyExecutor next) {
        super(next);
    }

    @Override
    protected boolean strategyCheck(CheckData checkData, Strategy strategy) {

        if (strategy.getVisitThreshold() < 0) {
            /**表示未配置此降级策略，故 不走该策略*/
            return true;
        }

        /**注意，这里一定要将降级量减去，降级时产生的访问量要去掉*/
        //todo 为什么要减去降级量 ，答：降级时产生的访问量要去掉
        return (checkData.getVisitCount() - checkData.getDowngradeCount()) <= strategy.getVisitThreshold();
    }

    @Override
    protected DowngradeActionType getStrategyType() {
        return DowngradeActionType.VISIT;
    }
}
