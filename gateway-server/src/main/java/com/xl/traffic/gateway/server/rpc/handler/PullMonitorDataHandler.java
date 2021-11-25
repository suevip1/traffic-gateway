package com.xl.traffic.gateway.server.rpc.handler;


import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.dto.MonitorDTO;
import com.xl.traffic.gateway.core.enums.SerializeType;
import com.xl.traffic.gateway.core.serialize.ISerialize;
import com.xl.traffic.gateway.core.serialize.SerializeFactory;
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
    public void execute(RpcMsg rpcMsg, Channel channel) {
        MonitorDTO monitorDTO = MonitorReport.buildMonitorDTO();
        rpcMsg.setBody(iSerialize.serialize(monitorDTO));
        sendMsg(rpcMsg, channel);
    }


    /**
     * 异步发送
     *
     * @param rpcMsg
     * @return: void
     * @author: xl
     * @date: 2021/7/29
     **/
    public void sendMsg(RpcMsg rpcMsg, Channel channel) {
        if (channel.isWritable()) {
            channel.writeAndFlush(rpcMsg);
        } else {
            try {
                /**水位线不够时,需要同步发送，进行阻塞等待水位线下*/
                channel.writeAndFlush(rpcMsg).sync();
                log.info("publish  rpcMsg sended. remoteAddress:[{}], packet:[{}]", channel.remoteAddress(), rpcMsg);
            } catch (InterruptedException e) {
                log.info("write and flush msg exception. packet:[{}]", rpcMsg, e);
            }
        }
    }

}
