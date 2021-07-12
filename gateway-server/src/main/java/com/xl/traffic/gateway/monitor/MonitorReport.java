package com.xl.traffic.gateway.monitor;

import com.sun.management.OperatingSystemMXBean;
import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.dto.MonitorDTO;
import com.xl.traffic.gateway.core.enums.MsgAppNameType;
import com.xl.traffic.gateway.core.enums.MsgCMDType;
import com.xl.traffic.gateway.core.enums.MsgGroupType;
import com.xl.traffic.gateway.core.enums.SerializeType;
import com.xl.traffic.gateway.core.metrics.MetricsMonitor;
import com.xl.traffic.gateway.core.model.SystemInfoModel;
import com.xl.traffic.gateway.core.serialize.ISerialize;
import com.xl.traffic.gateway.core.serialize.Protostuff;
import com.xl.traffic.gateway.core.serialize.SerializeFactory;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.core.utils.SnowflakeIdWorker;
import com.xl.traffic.gateway.rpc.client.RpcClient;
import com.xl.traffic.gateway.rpc.pool.NodePoolManager;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 监控上报
 *
 * @author: xl
 * @date: 2021/7/9
 **/
public class MonitorReport {

    static ISerialize iSerialize = SerializeFactory.getInstance().getISerialize(SerializeType.protobuf);


    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);


    /**
     * 上报 qps，流量，服务，内存使用率，cpu使用率等
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/7/7
     **/
    public static void statisticsReportMonitorData() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            /**构建监控上报数据*/
            MonitorDTO monitorDTO = MonitorDTO.builder()
                    .bytes(MetricsMonitor.getBytes().get())
                    .qps(MetricsMonitor.getQps().get())
                    .serverName(GatewayConstants.GATEWAY + GatewayConstants.SEQ + AddressUtils.getInnetIp())
                    .systemInfoModel(getSystemInfo())
                    .build();
            RpcClient rpcClient = NodePoolManager.getInstance().chooseRpcClient(GatewayConstants.MONITOR_GROUP);
            RpcMsg rpcMsg = new RpcMsg(MsgCMDType.UPLOAD_MONITOR_DATA_CMD.getType(), MsgGroupType.MONITOR.getType(),
                    MsgAppNameType.MONITOR.getType(), SnowflakeIdWorker.getInstance().nextId(),
                    iSerialize.serialize(monitorDTO), (byte) 0);
            rpcClient.sendAsync(rpcMsg);
        }, 0, GatewayConstants.REPORT_GATEWAY_HEALTH_DATA_TIME * 1000, TimeUnit.MILLISECONDS);


    }

    /**
     * 获取系统信息
     *
     * @param
     * @return: com.xl.traffic.gateway.core.model.SystemInfoModel
     * @author: xl
     * @date: 2021/7/7
     **/
    public static SystemInfoModel getSystemInfo() {

        OperatingSystemMXBean mem = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double processCpuLoad = mem.getProcessCpuLoad();
        double systemCpuLoad = mem.getSystemCpuLoad();
        long processCpuTime = mem.getProcessCpuTime();
        // 虚拟机级内存情况查询
        long vmFree = 0;
        long vmUse = 0;
        long vmTotal = 0;
        long vmMax = 0;
        int byteToMb = 1024 * 1024;
        Runtime rt = Runtime.getRuntime();
        vmTotal = rt.totalMemory() / byteToMb;
        vmFree = rt.freeMemory() / byteToMb;
        vmMax = rt.maxMemory() / byteToMb;
        vmUse = vmTotal - vmFree;

        SystemInfoModel systemInfoModel = SystemInfoModel.builder()
                .processCpuLoad(processCpuLoad)
                .systemCpuLoad(systemCpuLoad)
                .vmFree(vmFree)
                .vmMax(vmMax)
                .vmTotal(vmTotal)
                .vmUse(vmUse)
                .build();
        return systemInfoModel;
    }
}
