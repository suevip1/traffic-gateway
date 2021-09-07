package com.xl.traffic.gateway.server.rpc.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.enums.SerializeType;
import com.xl.traffic.gateway.core.serialize.ISerialize;
import com.xl.traffic.gateway.core.serialize.SerializeFactory;
import com.xl.traffic.gateway.hystrix.model.PushResponse;
import com.xl.traffic.gateway.hystrix.service.PullAndPushService;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 降级数据通知,一条一条通知,web端页面新增/修改触发
 *
 * @author: xl
 * @date: 2021/9/7
 **/
@Component
@Slf4j
public class HystrixNotifyHandler implements GatewayRpcServerHandlerService {

    ISerialize iSerialize = SerializeFactory.getInstance().getISerialize(SerializeType.protobuf);


    @Override
    public void execute(RpcMsg rpcMsg, Channel channel) {
        PushResponse response = iSerialize.deserialize(rpcMsg.getBody(), PushResponse.class);
        PullAndPushService.getInstance().updateHystrix(response);

    }
}
