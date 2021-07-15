package com.xl.traffic.gateway.hystrix.strategy;

import com.xl.traffic.gateway.hystrix.enums.DowngradeStrategyType;
import com.xl.traffic.gateway.hystrix.model.CheckData;
import com.xl.traffic.gateway.hystrix.model.Strategy;
import lombok.Getter;
import lombok.Setter;

/**
 * 策略执行链
 *
 * @author: xl
 * @date: 2021/6/24
 **/
public abstract class AbstractStrategyExecutor {


    /**
     * 当前策略的下一个策略
     */
    @Getter
    @Setter
    protected AbstractStrategyExecutor next;

    public AbstractStrategyExecutor(AbstractStrategyExecutor next) {
        this.next = next;
    }


    /**
     * 策略检查
     *
     * @param checkData 当前数据
     * @param strategy  当前策略
     * @return: boolean true:通过，不用降级；false:不通过，需要降级
     * @author: xl
     * @date: 2021/6/24
     **/
    protected abstract boolean strategyCheck(CheckData checkData, Strategy strategy);

    /**
     * 获取策略类型
     *
     * @param
     * @return: com.xl.traffic.gateway.hystrix.enums.DowngradeActionType
     * @author: xl
     * @date: 2021/6/24
     **/
    protected abstract DowngradeStrategyType getStrategyType();


    /**
     * 开始执行策略链
     *
     * @param checkData
     * @param strategy
     * @return: DowngradeActionType null-通过检查，不用降级，!=null:没通过，降级触发的类型，进行降级
     * @author: xl
     * @date: 2021/6/24
     **/
    public DowngradeStrategyType execute(CheckData checkData, Strategy strategy) {
        boolean checkSuccess = strategyCheck(checkData, strategy);
        if (checkSuccess && next != null) {
            return next.execute(checkData, strategy);
        }
        if (!checkSuccess) {
            return getStrategyType();
        }
        return null;
    }


}
