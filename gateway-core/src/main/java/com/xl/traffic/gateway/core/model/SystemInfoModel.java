package com.xl.traffic.gateway.core.model;

import com.sun.management.OperatingSystemMXBean;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.management.ManagementFactory;

/**
 * 系统信息
 *
 * @author: xl
 * @date: 2021/7/7
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SystemInfoModel {

    /**
     * 进程cpu使用
     */
    private double processCpuLoad;
    /**
     * 系统cpu使用
     */
    private double systemCpuLoad;

    /**
     * jvm空闲
     */
    private long vmFree = 0;
    /**
     * jvm使用
     */
    private long vmUse = 0;
    /**
     * jvm总量
     */
    private long vmTotal = 0;
    /**
     * jvm最大
     */
    private long vmMax = 0;
}
