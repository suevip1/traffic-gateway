package com.xl.traffic.gateway.hystrix.counter;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 用于并发量统计的数据
 *
 * @author: xl
 * @date: 2021/6/24
 **/
public class ConcurrentData {


    /**
     * 并发量现值，将并发严格限制在阈值之内
     */
    private volatile Semaphore concurrentLimit;

    /**
     * 并发量阈值
     */
    private volatile int cocurrentThreshold;

    /**为啦保证acquire 和release 使用的是同一个concurrentLimit*/
    private ThreadLocal<SemaphoreResult> concurrentLimitThreadLocal = new ThreadLocal<SemaphoreResult>(){
        @Override
        protected SemaphoreResult initialValue() {
            return new SemaphoreResult();
        }
    };

    /**为啦精准的控制并发量，需使用到锁*/
    private ReentrantLock lock = new ReentrantLock();



    @Data
    private class SemaphoreResult {


        private Semaphore semaphore;

        private boolean acquireResult;


    }


}
