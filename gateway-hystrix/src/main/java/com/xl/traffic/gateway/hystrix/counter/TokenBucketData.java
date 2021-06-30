package com.xl.traffic.gateway.hystrix.counter;

import com.xl.traffic.gateway.hystrix.constant.DowngradeConstant;
import com.xl.traffic.gateway.hystrix.model.VisitValue;
import lombok.Getter;

/**
 * 令牌桶的实现 （可以认为是QPS的高级替换方案）
 * 速率：每秒生成多少令牌
 * 桶容量：桶里面最多能存出多少个令牌
 *
 * @author: xl
 * @date: 2021/6/25
 **/
public class TokenBucketData {

    /**
     * 令牌bucket桶的数量为10个
     * 令牌桶的时间宽度为1s
     * 令牌桶的周期数为3个滑动周期
     * 表示：速率为每秒最多生成10个令牌桶
     **/
    @Getter
    private AbstractCycleData cycletokenBucketData = new SlidingWindowData(
            DowngradeConstant.CYCLE_NUM, DowngradeConstant.CYCLE_BUCKET_NUM, DowngradeConstant
            .BUCKET_TIME);


    /**
     * 当前令牌桶使用计数器+1
     *
     * @param time
     * @return: long 当前桶的访问量
     * @author: xl
     * @date: 2021/6/25
     **/
    public long takeOneToken(long time) {
        VisitValue visitValue = cycletokenBucketData.incrementAndGet(time);
        return visitValue.getBucketValue();
    }
}
