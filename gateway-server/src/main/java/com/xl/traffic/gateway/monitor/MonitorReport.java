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
import com.xl.traffic.gateway.core.serialize.SerializeFactory;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.core.utils.SnowflakeIdWorker;
import com.xl.traffic.gateway.rpc.client.RpcClient;
import com.xl.traffic.gateway.rpc.pool.NodePoolManager;

import java.lang.management.ManagementFactory;

/**
 * 监控上报
 *
 * @author: xl
 * @date: 2021/7/9
 **/
public class MonitorReport {

    static ISerialize iSerialize = SerializeFactory.getInstance().getISerialize(SerializeType.protobuf);



    /**
     * 注册上报 qps，流量，服务，内存使用率，cpu使用率等
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/7/7
     **/
    public static void registerReportMonitorData() {

        MonitorDTO monitorDTO = MonitorDTO.builder()
                .gatewayIp(AddressUtils.getInnetIp())
                .serverName(GatewayConstants.GATEWAY)
                .group(GatewayConstants.GATEWAY_GROUP)
                .build();
        /**注册monitor任务*/
        Long reqId = SnowflakeIdWorker.getInstance().nextId();
        //todo monitor的话就不用集群啦，就一台服务器就可以啦，降低维护成本，就是一个监控，没啥压力
        RpcClient rpcClient = NodePoolManager.getInstance().chooseRpcClient(GatewayConstants.MONITOR_GROUP);
        RpcMsg rpcMsg = new RpcMsg(MsgCMDType.REGISTER_MONITOR_TASK.getType(), MsgGroupType.MONITOR.getType(),
                MsgAppNameType.MONITOR.getType(), reqId, iSerialize.serialize(monitorDTO));
        rpcClient.sendAsync(rpcMsg);
    }

    /**
     * 获取指标信息
     *
     * @param
     * @return: com.xl.traffic.gateway.core.dto.MonitorDTO
     * @author: xl
     * @date: 2021/9/6
     **/
    public static MonitorDTO buildMonitorDTO() {
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
        MonitorDTO monitorDTO = MonitorDTO.builder()
                .connectNum(MetricsMonitor.getConnectNum().get())
                .requestBytes(MetricsMonitor.getRequestBytes().get())
                .responseBytes(MetricsMonitor.getResponseBytes().get())
                .serverName(GatewayConstants.GATEWAY + GatewayConstants.SEQ + AddressUtils.getInnetIp())
                .systemInfoModel(systemInfoModel)
                .build();
        return monitorDTO;
    }
}
