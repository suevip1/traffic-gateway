package com.xl.traffic.gateway.core.helper;


import com.xl.traffic.gateway.core.cache.CaffineCacheUtil;
import com.xl.traffic.gateway.core.metrics.DDOSMetrics;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * ddos辅助类
 *
 * @author: xl
 * @date: 2021/7/12
 **/
@Slf4j
public class DDOSHelper {

    private static int CYCLE_BUCKET_NUM = 1;
    private static int BUCKET_TIME = 1;


    /**
     * 负责清理下一个周期的数据
     */
    private final static ScheduledExecutorService cleanAndUploadExecutor = new ScheduledThreadPoolExecutor(1,
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread("clean-ddosIPdata-thread");
                }
            });

    static {
        //todo 注意，这里不使用scheduleAtFixedRate，因为从长期角度来讲，scheduleAtFixedRate没有每次任务执完去计算下次执行时间准
        cleanAndUploadExecutor.schedule(new CleanNextDDOSMetricsData(), calDistanceNextExecuteTime(), TimeUnit.MILLISECONDS);
    }

    /**
     * 获取ddos的访问量的统计
     * 不存则新建
     *
     * @param ip
     * @return: com.xl.traffic.gateway.core.metrics.DDOSMetrics
     * @author: xl
     * @date: 2021/7/12
     **/
    public static DDOSMetrics getDDOSMetrics(String ip) {
        DDOSMetrics monitorMetrics = CaffineCacheUtil.getDdosMetricsCache().get(ip, key -> {
            return new DDOSMetrics(CYCLE_BUCKET_NUM, BUCKET_TIME);
        });
        return monitorMetrics;
    }


    /**
     * 校验ip是否超过限制
     *
     * @param ip
     * @return: long
     * @author: xl
     * @date: 2021/7/12
     **/
    public static boolean ddosIpLimit(String ip) {
        long now = System.currentTimeMillis();
        /**是否在黑名单中*/
        if (!StringUtils.isEmpty(CaffineCacheUtil.getBlackIpCacheMap().getIfPresent(ip))) {
            return true;
        }
        DDOSMetrics ddosMetrics = getDDOSMetrics(ip);
        if (ddosMetrics.getLastCycleHealthValue(now) >= ZKConfigHelper.getInstance().getGatewayCommonConfig().getIp_limit_counts()) {
            /**加入IP黑名单*/
            CaffineCacheUtil.getBlackIpCacheMap().put(ip, ip);
            return true;
        }
        getDDOSMetrics(ip).incrementAndGet(now);
        return false;
    }


    public static class CleanNextDDOSMetricsData implements Runnable {

        @Override
        public void run() {
            try {
                long now = System.currentTimeMillis();

                for (Map.Entry<String, DDOSMetrics> entry : CaffineCacheUtil.getDdosMetricsCache().asMap().entrySet()) {
                    DDOSMetrics ddosMetrics = entry.getValue();
                    /**
                     * 在每1秒钟的第2秒钟，即第2秒钟会开始清理所有滑动周期内CycleTimeData下一周期的数据
                     */
                    ddosMetrics.cleanNextHealthMetricsRecord(now);
                }
                /**
                 * 这里在计算下一个任务的执行时间点
                 * 因为对时间的精度要求比较高，所以不能用固定的周期方法
                 * 下一个清理任务有当前清理任务执行完成后算出来，长期来看，精准度会很高
                 * */
                cleanAndUploadExecutor.schedule(this,
                        calDistanceNextExecuteTime(), TimeUnit.MILLISECONDS);

            } catch (Exception ex) {
                log.error("CycleClearAndUploadTask#run has exception:" + ex.getMessage(), ex);
            }
        }
    }


    /**
     * 计算下一次任务执行的时间点
     *
     * @param
     * @return: long
     * @author: xl
     * @date: 2021/6/28
     **/
    public static long calDistanceNextExecuteTime() {
        long cycleTime = CYCLE_BUCKET_NUM * BUCKET_TIME * 1000;
        long now = System.currentTimeMillis();
        return (now - now % cycleTime) + cycleTime * 2;
    }


}
