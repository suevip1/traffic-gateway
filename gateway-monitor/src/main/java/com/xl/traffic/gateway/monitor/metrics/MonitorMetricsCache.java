package com.xl.traffic.gateway.monitor.metrics;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.core.gson.GSONUtil;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.hystrix.service.CycleDataService;
import com.xl.traffic.gateway.register.zookeeper.ZkHelp;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 监控服务健康缓存
 *
 * @author: xl
 * @date: 2021/7/9
 **/
@Slf4j
public class MonitorMetricsCache {


    private static int CYCLE_BUCKET_NUM = 3;
    private static int BUCKET_TIME = 10 * 60;

    /**
     * 非健康次数阈值=上报时间间隔/60*10/2+2
     * 当N分钟内，非健康错误率>70% 进行降级
     */
    private static int healthThreshold = GatewayConstants.REPORT_GATEWAY_HEALTH_DATA_TIME / 60 * 10 / 2 + 2;

    /**
     * 负责清理下一个周期的数据
     */
    private final static ScheduledExecutorService cleanAndUploadExecutor = new ScheduledThreadPoolExecutor(1,
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread("clean-healthdata-thread");
                }
            });


    static {
        //todo 注意，这里不使用scheduleAtFixedRate，因为从长期角度来讲，scheduleAtFixedRate没有每次任务执完去计算下次执行时间准
        cleanAndUploadExecutor.schedule(new CleanNextHealthMetricsData(), calDistanceNextExecuteTime(), TimeUnit.MILLISECONDS);
    }

    /**
     * 本地缓存，数据结构:ip-MonitorMetrics
     */
    static Cache<String, MonitorMetrics> ipMonitorMetricsCache =
            Caffeine.newBuilder()
                    .maximumSize(GatewayConstants.CONNECT_CACHE_MAX_SIZE)
                    .build();
    /**
     * 本地缓存，数据结构:ip-weight
     * 存储每个节点的原始权重值
     */
    static Cache<String, Integer> ipWeightCache =
            Caffeine.newBuilder()
                    .maximumSize(GatewayConstants.CONNECT_CACHE_MAX_SIZE)
                    .build();

    /**
     * 获取监控健康指标统计
     * 不存则新建
     *
     * @param ip 服务ip
     * @return: com.xl.traffic.gateway.monitor.metrics.MonitorMetrics
     * @author: xl
     * @date: 2021/7/9
     **/
    public static MonitorMetrics getMonitorMetrics(String ip) {
        MonitorMetrics monitorMetrics = ipMonitorMetricsCache.get(ip, key -> {
            return new MonitorMetrics(CYCLE_BUCKET_NUM, BUCKET_TIME);
        });
        return monitorMetrics;
    }

    public static class CleanNextHealthMetricsData implements Runnable {

        @Override
        public void run() {
            try {
                long now = System.currentTimeMillis();

                for (Map.Entry<String, MonitorMetrics> entry : ipMonitorMetricsCache.asMap().entrySet()) {

                    MonitorMetrics monitorMetrics = entry.getValue();
                    String ip = entry.getKey();
                    /**
                     * 在每10分钟的第5分钟，即第15分钟会开始清理所有滑动周期内CycleTimeData下一周期的数据
                     */
                    monitorMetrics.cleanNextHealthMetricsRecord(now);

                    /**获取上一周期的数据，是否大于阈值，大于的话 进行 降级处理*/
                    long lastHealthCycleReportDataCount = monitorMetrics.getLastCycleHealthValue(now);
                    String dataStr = ZkHelp.getInstance().getValue(GatewayConstants.GATEWAY_ZK_ROOT_PATH + "/" + ip);
                    ServerNodeInfo nodeInfo = GSONUtil.fromJson(dataStr, ServerNodeInfo.class);
                    int weight = nodeInfo.getWeight();

                    if (lastHealthCycleReportDataCount >= healthThreshold) {
                        /**进行服务降级*/
                        int finalWeight = weight;
                        ipWeightCache.get(ip, key -> {
                            return finalWeight;
                        });
                        /**每次都减20%流量*/
                        weight = weight - 20;
                        if (weight <= 0) {
                            /**不打到这台机器啦*/
                            weight = 0;
                        }
                        nodeInfo.setWeight(weight);
                    } else {
                        /**流量降级恢复*/
                        nodeInfo.setWeight(ipWeightCache.getIfPresent(ip));
                    }
                    ZkHelp.getInstance().setPathData(GatewayConstants.GATEWAY_ZK_ROOT_PATH + ip, GSONUtil.toJson(nodeInfo));
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
        return (now - now % cycleTime) + cycleTime / 2;
    }


}