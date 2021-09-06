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


    /**
     * 注册监控任务
     *
     * @param monitorDTO
     * @return: void
     * @author: xl
     * @date: 2021/7/9
     **/
    public void registerMonitorTask(MonitorDTO monitorDTO) {
        MonitorMetricsCache.registerMonitorMetrics(monitorDTO.getGatewayIp(),monitorDTO.getServerName(),monitorDTO.getGroup());
    }




}
