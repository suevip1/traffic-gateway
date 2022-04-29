package com.xl.traffic.gateway.core.metrics;

import com.google.common.util.concurrent.AtomicDouble;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 指标监控
 * 进出口流量
 * 进出口qps
 *
 * @author: xl
 * @date: 2021/7/7
 **/
public class MetricsMonitor {

    /**连接数*/
    @Getter
    private static AtomicInteger connectNum = new AtomicInteger(0);
    @Getter
    private static AtomicDouble requestBytes = new AtomicDouble(0d);
    @Getter
    private static AtomicDouble responseBytes = new AtomicDouble(0d);


}
