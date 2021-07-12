package com.xl.traffic.gateway.monitor.server.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.dto.MonitorDTO;
import com.xl.traffic.gateway.core.dto.RouterDTO;
import com.xl.traffic.gateway.core.enums.SerializeType;
import com.xl.traffic.gateway.core.gson.GSONUtil;
import com.xl.traffic.gateway.core.serialize.ISerialize;
import com.xl.traffic.gateway.core.serialize.Protostuff;
import com.xl.traffic.gateway.core.serialize.SerializeFactory;
import com.xl.traffic.gateway.core.thread.ThreadPoolExecutorUtil;
import com.xl.traffic.gateway.monitor.service.MonitorMetricsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.channels.Channel;

/**
 * gateway 健康数据上报处理
 *
 * @author: xl
 * @date: 2021/7/5
 **/
@Component
@Slf4j
public class MonitorReportDataHandler implements MonitorServerHandlerService {

    ISerialize iSerialize = SerializeFactory.getInstance().getISerialize(SerializeType.protobuf);
    @Autowired
    MonitorMetricsService monitorMetricsService;

    @Override
    public void execute(RpcMsg rpcMsg, Channel channel) {
        MonitorDTO monitorDTO = iSerialize.deserialize(rpcMsg.getBody(), MonitorDTO.class);
        ThreadPoolExecutorUtil.getCommonIOPool().submit(() -> {
            monitorMetricsService.exeuteHealthMetricsData(monitorDTO);
            log.info("reciver server :{},report health data:{}", monitorDTO.getGatewayIp(), GSONUtil.toJson(monitorDTO));
        });
    }
}
