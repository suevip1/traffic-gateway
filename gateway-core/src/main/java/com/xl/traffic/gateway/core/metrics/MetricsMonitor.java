package com.xl.traffic.gateway.core.metrics;

import com.google.common.util.concurrent.AtomicDouble;
import com.sun.management.OperatingSystemMXBean;
import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.dto.MonitorDTO;
import com.xl.traffic.gateway.core.model.SystemInfoModel;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import lombok.Getter;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 指标监控
 *
 * @author: xl
 * @date: 2021/7/7
 **/
public class MetricsMonitor {

    @Getter
    private static AtomicInteger qps = new AtomicInteger(0);
    @Getter
    private static AtomicDouble bytes = new AtomicDouble(0d);


}
