package com.xl.traffic.gateway.monitor.server.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.dto.MonitorDTO;
import com.xl.traffic.gateway.core.enums.SerializeType;
import com.xl.traffic.gateway.core.serialize.ISerialize;
import com.xl.traffic.gateway.core.serialize.SerializeFactory;
import com.xl.traffic.gateway.core.thread.ThreadPoolExecutorUtil;
import com.xl.traffic.gateway.monitor.service.MonitorMetricsService;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 注册 健康数据上报处理
 *
 * @author: xl
 * @date: 2021/7/5
 **/
@Component
@Slf4j
public class RegisterMonitorDataHandler implements MonitorServerHandlerService {

    ISerialize iSerialize = SerializeFactory.getInstance().getISerialize(SerializeType.protobuf);
    @Autowired
    MonitorMetricsService monitorMetricsService;

    @Override
    public void execute(RpcMsg rpcMsg, Channel channel) {
        MonitorDTO monitorDTO = iSerialize.deserialize(rpcMsg.getBody(), MonitorDTO.class);
        ThreadPoolExecutorUtil.getGateway_Register_Pool().submit(() -> {
            monitorMetricsService.registerMonitorTask(monitorDTO);
            log.info("reciver register monitor serverName :{},ip:{}", monitorDTO.getServerName(),monitorDTO.getGatewayIp());
        });
    }
}
