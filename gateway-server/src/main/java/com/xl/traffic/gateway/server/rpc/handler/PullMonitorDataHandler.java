package com.xl.traffic.gateway.server.rpc.handler;


import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.dto.MonitorDTO;
import com.xl.traffic.gateway.core.enums.SerializeType;
import com.xl.traffic.gateway.core.serialize.ISerialize;
import com.xl.traffic.gateway.core.serialize.SerializeFactory;
import com.xl.traffic.gateway.core.server.connection.Connection;
import com.xl.traffic.gateway.monitor.MonitorReport;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * 拉取本地指标健康数据
 *
 * @author: xl
 * @date: 2021/7/5
 **/
@Slf4j
@Component
public class PullMonitorDataHandler implements GatewayRpcServerHandlerService {

    ISerialize iSerialize = SerializeFactory.getInstance().getISerialize(SerializeType.protobuf);

    @Override
    public void execute(RpcMsg rpcMsg, Connection connection) {
        MonitorDTO monitorDTO = MonitorReport.buildMonitorDTO();
        rpcMsg.setBody(iSerialize.serialize(monitorDTO));
        connection.sendMsg(rpcMsg);
    }


}
