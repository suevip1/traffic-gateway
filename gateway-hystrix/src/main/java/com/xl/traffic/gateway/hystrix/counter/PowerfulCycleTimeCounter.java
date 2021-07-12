package com.xl.traffic.gateway.hystrix.counter;

import com.xl.traffic.gateway.core.counter.AbstractCycleData;
import com.xl.traffic.gateway.core.model.VisitValue;
import com.xl.traffic.gateway.hystrix.service.CycleDataService;

/**
 * 包含访问量、并发量、异常量、异常率、超时等特性的毫秒计数器
 *
 * @author: xl
 * @date: 2021/6/25
 **/
public class PowerfulCycleTimeCounter {


    /**
     * 秒访问量统计数据
     */
    private AbstractCycleData visitData = CycleDataService.createCycleData();
    /**
     * 并发量统计数据
     */
    private ConcurrentData concurrentData = CycleDataService.createConcurrentCycleData();
    /**
     * 秒异常访问量统计数据
     */
    private AbstractCycleData exceptionData = CycleDataService.createCycleData();
    /**
     * 超时计数器统计数据
     */
    private AbstractCycleData timeoutData = CycleDataService.createCycleData();
    /**
     * 令牌桶统计数据
     */
    private TokenBucketData tokenBucketData = CycleDataService.createTokenBucketCycleData();

    /**
     * 降级计数器
     */
    private AbstractCycleData downgradeData = CycleDataService.createCycleData();

    /**
     * 秒访问量计数器+1
     *
     * @param time
     * @return: com.xl.traffic.gateway.hystrix.model.VisitValue
     * @author: xl
     * @date: 2021/6/25
     **/
    public VisitValue visitAddAndGet(long time) {
        return commonAddAndGet(time, visitData);
    }

    /**
     * 获取秒访问量上一秒的桶值的访问量
     *
     * @param time
     * @return: 返回上一秒的访问量
     * @author: xl
     * @date: 2021/6/25
     **/
    public long getLastSecondVisitBucketValue(long time) {
        return visitData.getBucketValue(time - 1000);
    }

    /**
     * 获取上一周期的秒访问调用量的统计值
     *
     * @param time
     * @return: 返回上一周期的调用秒访问量总值
     * @author: xl
     * @date: 2021/6/25
     **/
    public long getLastCycleVisitValue(long time) {
        return visitData.getLastWholeCycleValue(time);
    }

    /**
     * 并发调用量当前毫秒的计数器加1
     *
     * @param time
     * @return: boolean false 超出并发量阈值 true 允许通过
     * @author: xl
     * @date: 2021/6/25
     **/
    public boolean concurrentAcquire(long time) {
        return concurrentData.concurrentAcquire(time);
    }


    /**
     * 并发调用量当前毫秒的计数器减1
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/6/25
     **/
    public void concurrentRelease() {
        concurrentData.concurrentRelease();
    }


    /**
     * 获取上一周期并发量统计值
     *
     * @param time
     * @return: long 返回上一周期的调用并发量统计值
     * @author: xl
     * @date: 2021/6/25
     **/
    public int getLastCycleConcurrentValue(long time) {
        return concurrentData.getLastConcurrentMatCount(time);
    }


    /**
     * 设置异常调用量当前毫秒计数器+1
     *
     * @param time
     * @return: long  返回当前周期内的异常总次数
     * @author: xl
     * @date: 2021/6/25
     **/
    public long exceptionAddAndGet(long time) {
        return commonAddAndGet(time, exceptionData).getSlidingCycleValue();
    }

    /**
     * 获取指定时间的异常调用量的值
     *
     * @param time
     * @return: long 当前时间的桶的调用的异常次数
     * @author: xl
     * @date: 2021/6/25
     **/
    public long getExceptionValue(long time) {
        return exceptionData.getBucketValue(time);
    }

    /**
     * 获取上一周期的异常调用量的统计值
     *
     * @param time
     * @return: long  返回上一周期的异常调用量总次数
     * @author: xl
     * @date: 2021/6/25
     **/
    public long getLastExceptionCycleValue(long time) {
        return exceptionData.getLastWholeCycleValue(time);
    }

    /**
     * 设置超时时间的调用次数+1
     *
     * @param time
     * @return: 返回当前周期内的超时调用总次数
     * @author: xl
     * @date: 2021/6/25
     **/
    public long timeoutAddAndGet(long time) {
        return commonAddAndGet(time, timeoutData).getSlidingCycleValue();
    }

    /**
     * 获取指定时间的超时调用次数
     *
     * @param time
     * @return: 返回指定时间内的所在桶的超时调用次数
     * @author: xl
     * @date: 2021/6/25
     **/
    public long getTimeoutValue(long time) {
        return timeoutData.getBucketValue(time);
    }

    /**
     * 获取上一周期的超时调用总次数
     *
     * @param time
     * @return: 返回上一周期的超时调用总次数
     * @author: xl
     * @date: 2021/6/25
     **/
    public long getLastTimeoutCycleValue(long time) {
        return timeoutData.getLastWholeCycleValue(time);
    }

    /**
     * 尝试消耗一个令牌
     *
     * @param time
     * @return: 返回当前时间所在桶的消耗令牌个数
     * @author: xl
     * @date: 2021/6/25
     **/
    public long tokenBucketInvokeAddAndGet(long time) {
        return tokenBucketData.takeOneToken(time);
    }

    /**
     * 降级量的当前毫秒计数器+1
     *
     * @param time
     * @return: 返回当前周期内所有桶的降级总次数
     * @author: xl
     * @date: 2021/6/25
     **/
    public long downgrateAddAndGet(long time) {
        return commonAddAndGet(time, downgradeData).getSlidingCycleValue();
    }

    /**
     * 获取指定时间的周期内的降级总次数
     *
     * @param time
     * @return: 返回指定指定时间周期内的调用的降级总次数
     * @author: xl
     * @date: 2021/6/25
     **/
    public long getDowngrateValue(long time) {
        return downgradeData.getCurSlidingCycleValue(time);
    }


    /**
     * 获取上一周期的降级调用次数
     *
     * @param time
     * @return: 返回上一周期内的降级调用总次数
     * @author: xl
     * @date: 2021/6/25
     **/
    public long getLastDowngrateCycleValue(long time) {
        return downgradeData.getLastWholeCycleValue(time);
    }

    /**
     * 更新并发调用量的阈值
     *
     * @param concurrentThreshold
     * @return: void
     * @author: xl
     * @date: 2021/6/25
     **/
    public void updateConcurrentThreshold(int concurrentThreshold) {
        concurrentData.updateConcurrentThreshold(concurrentThreshold);
    }


    /**
     * 计数器加1的通用操作
     *
     * @param time
     * @param cycleData 计数器统计对象
     * @return: com.xl.traffic.gateway.hystrix.model.VisitValue
     * @author: xl
     * @date: 2021/6/25
     **/
    private VisitValue commonAddAndGet(long time, AbstractCycleData cycleData) {
        return cycleData.incrementAndGet(time);
    }


}
