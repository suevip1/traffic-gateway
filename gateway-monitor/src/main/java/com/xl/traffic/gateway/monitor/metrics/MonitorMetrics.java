package com.xl.traffic.gateway.monitor.metrics;


import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.hystrix.constant.DowngradeConstant;
import com.xl.traffic.gateway.hystrix.counter.AbstractCycleData;
import com.xl.traffic.gateway.hystrix.counter.SlidingWindowData;
import com.xl.traffic.gateway.hystrix.dispatch.DowngrateDispatcher;
import com.xl.traffic.gateway.hystrix.service.CycleDataService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


/**
 * 集群健康数据统计处理
 *
 * @author: xl
 * @date: 2021/7/9
 **/

@Slf4j
public class MonitorMetrics {

    /**
     * 我们只需要统计周期内的非健康指标，3个桶进行计算,10分钟
     * 滑动窗口
     */
    @Getter
    private AbstractCycleData healthMetricsCycleData;

    public MonitorMetrics(int CYCLE_BUCKET_NUM, int BUCKET_TIME) {
        healthMetricsCycleData = new SlidingWindowData(DowngradeConstant.CYCLE_NUM,
                CYCLE_BUCKET_NUM, BUCKET_TIME);
    }


    /**
     * 非健康指标+1
     *
     * @param now
     * @return: void
     * @author: xl
     * @date: 2021/7/9
     **/
    public void incrementAndGet(long now) {
        healthMetricsCycleData.incrementAndGet(now);
    }

    /**
     * 获取上一周期的非健康指标的统计值
     *
     * @param time
     * @return: 获取上一周期的非健康指标的统计值
     * @author: xl
     * @date: 2021/6/25
     **/
    public long getLastCycleHealthValue(long time) {
        return healthMetricsCycleData.getLastWholeCycleValue(time);
    }


    /**
     * 清理下一周期的滑动窗口数据
     *
     * @param time
     * @return: void
     * @author: xl
     * @date: 2021/7/9
     **/
    public void cleanNextHealthMetricsRecord(long time) {
        healthMetricsCycleData.clearNextCycleValue(time);
    }


}