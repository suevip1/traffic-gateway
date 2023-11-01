package com.xl.traffic.gateway.monitor.task;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.config.MonitorMetricsConfig;
import com.xl.traffic.gateway.core.dto.MonitorDTO;
import com.xl.traffic.gateway.core.enums.MsgAppNameType;
import com.xl.traffic.gateway.core.enums.MsgCMDType;
import com.xl.traffic.gateway.core.enums.MsgGroupType;
import com.xl.traffic.gateway.core.enums.SerializeType;
import com.xl.traffic.gateway.core.helper.ZKConfigHelper;
import com.xl.traffic.gateway.core.serialize.ISerialize;
import com.xl.traffic.gateway.core.serialize.SerializeFactory;
import com.xl.traffic.gateway.core.utils.RetryHelper;
import com.xl.traffic.gateway.core.utils.SnowflakeIdWorker;
import com.xl.traffic.gateway.monitor.metrics.MonitorMetrics;
import com.xl.traffic.gateway.monitor.metrics.MonitorMetricsCache;
import com.xl.traffic.gateway.monitor.start.MonitorStart;
import com.xl.traffic.gateway.rpc.client.RpcClient;
import com.xl.traffic.gateway.rpc.pool.NodePoolManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Slf4j
@SuppressWarnings(value = {"unchecked"})
public class MonitorTask {


    private static class InstanceHolder {
        public static final MonitorTask instance = new MonitorTask();
    }

    public static MonitorTask getInstance() {
        return MonitorTask.InstanceHolder.instance;
    }

    /**
     * 不适合在server上开定时器传送，会影响server 性能，本来monitor服务是离线监控服务，可以有能力做定时监控，减轻服务端的压力,提升性能
     */
    static ISerialize iSerialize = SerializeFactory.getInstance().getISerialize(SerializeType.protobuf);
    /**
     * 负责清理下一个周期的数据
     */
    private final static ScheduledExecutorService pullHealthDataExecutor = new ScheduledThreadPoolExecutor(1,
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread("pull-healthdata-thread");
                }
            });


    public void schedulePullServerHealthMetricsTask() {
        /**每10分钟获取一次*/
        pullHealthDataExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                pullServerHealthData();
            }
        }, 0, 10, TimeUnit.MINUTES);
    }


    /**
     * 获取服务的健康数据
     */
    public static void pullServerHealthData() {
        for (Map.Entry<String, MonitorMetrics> stringMonitorMetricsEntry : MonitorMetricsCache.ipMonitorMetricsCache.asMap().entrySet()) {
            String ip = stringMonitorMetricsEntry.getKey();
            String serverName = stringMonitorMetricsEntry.getValue().getServerName();
            String group = stringMonitorMetricsEntry.getValue().getGroup();
            RpcClient rpcClient = NodePoolManager.getInstance().chooseRpcClient(group, ip);
            if (rpcClient == null) {
                log.info("group:{},serverName:{},ip:{} no active rpcClient!!!", group, serverName, ip);
                continue;
            }
            RpcMsg rpcMsg = new RpcMsg(MsgCMDType.PULL_GATEWAY_HEALTH_DATA_CMD.getType(), MsgGroupType.valueOf(group).getType(),
                    MsgAppNameType.valueOf(serverName).getType(), SnowflakeIdWorker.getInstance().nextId(), null);
            try {
                // TODO: 2023/11/1 重试次数改成从配置中拿去
                RpcMsg result=RetryHelper.retryWithReturn(3l, 1000l, () -> {
                    try {
                        RpcMsg rsMsg = rpcClient.sendSync(rpcMsg, 500);
                        if (rsMsg.getBody() != null) {
                            MonitorDTO monitorDTO = iSerialize.deserialize(rpcMsg.getBody(), MonitorDTO.class);
                            exeuteHealthMetricsData(monitorDTO);
                            log.info("group:{},serverName:{},ip:{} get health report data success!!!", group, serverName, ip);
                            return rsMsg;
                        }
                        return null;
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                });
                if (result==null) {
                    /**表示该服务已经出现了问题,直接对该服务直接将weight权重置为0,阻挡新流量*/
                    // TODO: 2023/11/1

                }
            } catch (Exception exception) {
                log.error("rpc request error! errMsg:{}", exception);
            }
        }
    }

    /**
     * 处理健康上报数据逻辑
     *
     * @param monitorDTO
     * @return: void
     * @author: xl
     * @date: 2021/7/9
     **/
    public static void exeuteHealthMetricsData(MonitorDTO monitorDTO) {
        /**指标是否超出阈值
         * 1，连接数
         * 2，cpu使用率
         * 3，内存使用率
         * */
        if (
                monitorDTO.getConnectNum() >= ZKConfigHelper.getInstance().getMonitorMetricsConfig().getConnectNum()
                        || monitorDTO.getSystemInfoModel().getProcessCpuLoad() >= ZKConfigHelper.getInstance().getMonitorMetricsConfig().getCpuThreshold()
                        || monitorDTO.getSystemInfoModel().getVmUse() >= ZKConfigHelper.getInstance().getMonitorMetricsConfig().getMemoryThreshold()) {
            /**非健康指数+1*/
            MonitorMetricsCache.getMonitorMetrics(monitorDTO.getGatewayIp()).incrementAndGet(System.currentTimeMillis());
        }
    }
}
