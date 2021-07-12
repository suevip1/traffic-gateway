package com.xl.traffic.gateway.core.metrics;

import com.xl.traffic.gateway.core.counter.AbstractCycleData;
import com.xl.traffic.gateway.core.counter.SlidingWindowData;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import lombok.Getter;

/**
 * DDOS滑动窗口统计
 *
 * @author: xl
 * @date: 2021/7/12
 **/
public class DDOSMetrics {


    /**
     * 我们只需要统计周期内的ip访问量，1个桶进行计算,1秒钟
     * 滑动窗口
     */
    @Getter
    private AbstractCycleData ddosMetricsCycleData;

    public DDOSMetrics(int CYCLE_BUCKET_NUM, int BUCKET_TIME) {
        ddosMetricsCycleData = new SlidingWindowData(GatewayConstants.CYCLE_NUM,
                CYCLE_BUCKET_NUM, BUCKET_TIME);
    }


    /**
     * 访问量+1
     *
     * @param now
     * @return: void
     * @author: xl
     * @date: 2021/7/9
     **/
    public void incrementAndGet(long now) {
        ddosMetricsCycleData.incrementAndGet(now);
    }

    /**
     * 获取上一周期ip访问量的统计值
     *
     * @param time
     * @return: 获取上一周期ip访问量的统计值
     * @author: xl
     * @date: 2021/6/25
     **/
    public long getLastCycleHealthValue(long time) {
        return ddosMetricsCycleData.getLastWholeCycleValue(time);
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
        ddosMetricsCycleData.clearNextCycleValue(time);
    }


}
