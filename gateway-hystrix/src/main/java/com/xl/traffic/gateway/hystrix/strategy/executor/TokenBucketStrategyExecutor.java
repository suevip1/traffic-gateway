package com.xl.traffic.gateway.hystrix.strategy.executor;

import com.xl.traffic.gateway.hystrix.constant.DowngradeConstant;
import com.xl.traffic.gateway.hystrix.enums.DowngradeStrategyType;
import com.xl.traffic.gateway.hystrix.model.CheckData;
import com.xl.traffic.gateway.hystrix.model.Strategy;
import com.xl.traffic.gateway.hystrix.strategy.AbstractStrategyExecutor;
import org.springframework.core.annotation.Order;

/**
 * 令牌桶降级策略
 *
 * @author: xl
 * @date: 2021/6/24
 **/
@Order(2)
public class TokenBucketStrategyExecutor extends AbstractStrategyExecutor {

    public TokenBucketStrategyExecutor(AbstractStrategyExecutor next) {
        super(next);
    }

    @Override
    protected boolean strategyCheck(CheckData checkData, Strategy strategy) {

        if (strategy.getTokenBucketGeneratedTokenInSecond() < 0) {
            /**表示未配置此降级策略，故 不走该策略*/
            return true;
        }
        /**如果当前桶的令牌还没用完，那么直接返回true ，放行*/
        if (checkData.getTakeTokenBucketNum() <= strategy.getTokenBucketGeneratedTokenInSecond()) {
            return true;
        }
        /**如果当前桶的令牌已经用完，但又没额外设置桶的容量（即默认桶容量和桶每秒生成的令牌数相同），那就直接拒绝*/
        if (strategy.getTokenBucketSize() <= strategy.getTokenBucketGeneratedTokenInSecond()) {
            return false;
        }
        /**每个桶可用的令牌数不能超过桶的容量，超过的话直接决绝*/
        if (checkData.getTakeTokenBucketNum() > strategy.getTokenBucketSize()) {
            return false;
        }
        //桶的数量*桶的步长(秒)*每秒生成令牌数
        //已产生的令牌数
        //已产生的降级次数

        //todo 为什么要加降级次数，不懂，答：降级过程中 产生的令牌数，需要加回来
        /**如果当前秒的令牌已经不够用，那么就看历史桶中是否有剩余令牌匀一下*/
        return DowngradeConstant.CYCLE_BUCKET_NUM * DowngradeConstant.BUCKET_TIME * strategy.getTokenBucketGeneratedTokenInSecond() -
                checkData.getTakeTokenBucketNum() + checkData.getDowngradeCount() > 0;
    }

    @Override
    protected DowngradeStrategyType getStrategyType() {
        return DowngradeStrategyType.TOKEN_BUCKET;
    }
}
