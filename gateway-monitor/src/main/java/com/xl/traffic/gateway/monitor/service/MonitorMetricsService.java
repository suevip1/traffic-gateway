package com.xl.traffic.gateway.monitor.service;

import com.xl.traffic.gateway.core.config.MonitorMetricsConfig;
import com.xl.traffic.gateway.core.dto.MonitorDTO;
import com.xl.traffic.gateway.monitor.metrics.MonitorMetricsCache;
import org.springframework.stereotype.Component;

/**
 * 健康数据上报处理
 *
 * @author: xl
 * @date: 2021/7/9
 **/
@Component
public class MonitorMetricsService {

    //todo 需要注意这块如何读取配置
    MonitorMetricsConfig monitorMetricsConfig;

    /**
     * 处理健康上报数据逻辑
     *
     * @param monitorDTO
     * @return: void
     * @author: xl
     * @date: 2021/7/9
     **/
    public void exeuteHealthMetricsData(MonitorDTO monitorDTO) {

        /**连接数是否超出阈值*/
        if (monitorDTO.getConnectNum() >= monitorMetricsConfig.getConnectNum()
                || monitorDTO.getRequestQps() >= monitorMetricsConfig.getQpsThreshold()
                || monitorDTO.getSystemInfoModel().getProcessCpuLoad() >= monitorMetricsConfig.getCpuThreshold()
                || monitorDTO.getSystemInfoModel().getVmUse() >= monitorMetricsConfig.getMemoryThreshold()) {
            /**非健康指数+1*/
            MonitorMetricsCache.getMonitorMetrics(monitorDTO.getGatewayIp()).incrementAndGet(System.currentTimeMillis());
        }
    }


}
