package com.xl.traffic.gateway.hystrix.strategy.executor;

import com.xl.traffic.gateway.hystrix.enums.DowngradeStrategyType;
import com.xl.traffic.gateway.hystrix.model.CheckData;
import com.xl.traffic.gateway.hystrix.model.Strategy;
import com.xl.traffic.gateway.hystrix.strategy.AbstractStrategyExecutor;
import org.springframework.core.annotation.Order;

/**
 * 异常率次数降级策略
 *
 * @author: xl
 * @date: 2021/6/24
 **/
@Order(6)
public class ExceptionRateStrategyExecutor extends AbstractStrategyExecutor {

    public ExceptionRateStrategyExecutor(AbstractStrategyExecutor next) {
        super(next);
    }

    @Override
    protected boolean strategyCheck(CheckData checkData, Strategy strategy) {

        //todo 不明白
        if (strategy.getExceptionRateStart() < 0 ||
                strategy.getExceptionRateThreshold() < 0 ||
                strategy.getExceptionRateThreshold() > 100 ||
                strategy.getExceptionRateStart() >= checkData.getVisitCount()) {
            /**表示未配置此降级策略，故 不走该策略*/
            return true;
        }
        /**在计算异常率时，分母不能直接用访问量，需要把降级数量异移除，由于降级时实际没有真正访问业务方法*/
        long inputTotal = checkData.getVisitCount() - checkData.getDowngradeCount();

        /**异常率=当前周期内异常数量/访问量*/
        return inputTotal == 0 || (checkData.getExceptionCount() + 0.0) * 100 / inputTotal < strategy.getExceptionRateThreshold();
    }

    @Override
    protected DowngradeStrategyType getStrategyType() {
        return DowngradeStrategyType.EXCEPTION_RATE;
    }
}
