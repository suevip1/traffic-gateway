package com.xl.traffic.gateway.core.config;

import lombok.Data;

/**
 * 监控指标的配置
 *
 * @author: xl
 * @date: 2021/7/9
 **/
@Data
public class MonitorMetricsConfig {


    /**
     * cpu阈值
     */
    private double cpuThreshold;

    /**
     * 内存阈值
     */
    private double memoryThreshold;


    /**
     * 服务qps阈值
     */
    private int qpsThreshold;

    /**
     * 连接数阈值
     */
    private int connectNum;


}


