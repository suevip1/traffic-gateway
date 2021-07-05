package com.xl.traffic.gateway.hystrix.strategy.executor;

import com.xl.traffic.gateway.hystrix.enums.DowngradeActionType;
import com.xl.traffic.gateway.hystrix.model.CheckData;
import com.xl.traffic.gateway.hystrix.model.Strategy;
import com.xl.traffic.gateway.hystrix.strategy.AbstractStrategyExecutor;
import org.springframework.core.annotation.Order;

/**
 * 熔断策略
 * 当打开熔断开关时，直接全部降级，不走业务逻辑，实施保护
 *
 * @author: xl
 * @date: 2021/6/24
 **/
@Order(1)
public class FuseStrategyExecutor extends AbstractStrategyExecutor {

    public FuseStrategyExecutor(AbstractStrategyExecutor next) {
        super(next);
    }

    @Override
    protected boolean strategyCheck(CheckData checkData, Strategy strategy) {
        if (strategy.getFuse_switch() < 0) {
            /**表示未配置此降级策略，故 不走该策略*/
            return true;
        }
        return false;
    }

    @Override
    protected DowngradeActionType getStrategyType() {
        return DowngradeActionType.FUSE;
    }
}
