package com.xl.traffic.gateway.hystrix.counter;

import com.xl.traffic.gateway.core.counter.AbstractCycleData;
import com.xl.traffic.gateway.core.counter.SlidingWindowData;
import com.xl.traffic.gateway.hystrix.constant.DowngradeConstant;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 用于并发量统计的数据
 *
 * @author: xl
 * @date: 2021/6/24
 **/
@Slf4j
public class ConcurrentData {


    /**
     * 并发量现值，将并发严格限制在阈值之内
     */
    private volatile Semaphore concurrentLimit;

    /**
     * 并发量阈值
     */
    private volatile int cocurrentThreshold;

    /**
     * 为啦保证acquire 和release 使用的是同一个concurrentLimit
     */
    private ThreadLocal<SemaphoreResult> concurrentLimitThreadLocal = new ThreadLocal<SemaphoreResult>() {
        @Override
        protected SemaphoreResult initialValue() {
            return new SemaphoreResult();
        }
    };

    /**
     * 为啦精准的控制并发量，需使用到锁
     */
    private ReentrantLock lock = new ReentrantLock();

    /**
     * 我们只需要统计周期内的并发最大值，所以一个周期不需要10个桶，1个桶就可以
     */
    @Getter
    private AbstractCycleData cycleMaxConcurrentData = new SlidingWindowData(DowngradeConstant.CYCLE_NUM, 1, 10);


    /**
     * 默认最大值
     */
    public ConcurrentData() {
        this(Integer.MAX_VALUE);
    }

    public ConcurrentData(int cocurrentThreshold) {
        this.cocurrentThreshold = cocurrentThreshold <= 0 ? Integer.MAX_VALUE : cocurrentThreshold;
        concurrentLimit = new Semaphore(this.cocurrentThreshold);
    }


    /**
     * 并发访问+1
     *
     * @param time
     * @return: boolean
     * @author: xl
     * @date: 2021/6/25
     **/
    public boolean concurrentAcquire(long time) {

        /**获取信号量访问令牌，获取成功 返回true，失败返回false*/
        boolean acquireResult = concurrentLimit.tryAcquire();
        /**设置当前线程的信号量*/
        concurrentLimitThreadLocal.get().setVisitResult(concurrentLimit, acquireResult);
        /**获取当前并发量=并发量阈值-可用令牌数*/
        int curConcurrentValue = cocurrentThreshold - concurrentLimit.availablePermits();
        /**统计历史最大并发量的值*/
        if (curConcurrentValue > cycleMaxConcurrentData.getCurSlidingCycleValue(time)) {
            try {
                if (lock.tryLock(3, TimeUnit.MILLISECONDS)) {
                    try {
                        if (curConcurrentValue > cycleMaxConcurrentData.getCurSlidingCycleValue(time)) {
                            cycleMaxConcurrentData.setBucketValue(time, curConcurrentValue);
                        }
                    } finally {
                        lock.unlock();
                    }
                } else {
                    log.warn("ConcurrentData#concurrentAcquire 获取锁失败，最大并发值可能统计不准.");
                }
            } catch (Exception ex) {

            }
        }
        return acquireResult;
    }

    /**
     * 并发量-1
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/6/25
     **/
    public void concurrentRelease() {
        /**阈值小于0 表示不做限制*/
        if (cocurrentThreshold < 0) {
            return;
        }
        if (concurrentLimitThreadLocal.get().isAcquireResult()) {
            Semaphore semaphore = concurrentLimitThreadLocal.get().getSemaphore();
            if (null != semaphore) {
                semaphore.release();
            }
        }

    }

    /**
     * 更新并发量阈值
     *
     * @param concurrentThreshold
     * @return: void
     * @author: xl
     * @date: 2021/6/25
     **/
    public void updateConcurrentThreshold(int concurrentThreshold) {
        this.cocurrentThreshold = concurrentThreshold;
        concurrentLimit = new Semaphore(concurrentThreshold < 0 ? 0 : concurrentThreshold);
    }

    /**
     * 获取上一周期的所有并发数
     *
     * @param time
     * @return: int
     * @author: xl
     * @date: 2021/6/25
     **/
    public int getLastConcurrentMatCount(long time) {
        return (int) cycleMaxConcurrentData.getLastWholeCycleValue(time);
    }


    @Data
    private class SemaphoreResult {


        private Semaphore semaphore;

        private boolean acquireResult;

        public void setVisitResult(Semaphore semaphore, boolean acquireResult) {
            this.acquireResult = acquireResult;
            this.semaphore = semaphore;
        }


    }


}
