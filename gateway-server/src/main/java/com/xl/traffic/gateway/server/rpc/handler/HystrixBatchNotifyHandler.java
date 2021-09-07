package com.xl.traffic.gateway.server.rpc.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.enums.SerializeType;
import com.xl.traffic.gateway.core.serialize.ISerialize;
import com.xl.traffic.gateway.core.serialize.SerializeFactory;
import com.xl.traffic.gateway.core.server.connection.Connection;
import com.xl.traffic.gateway.hystrix.model.PushResponse;
import com.xl.traffic.gateway.hystrix.service.PullAndPushService;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 降级数据通知,批量通知,当gateway 服务发生扩容/更新/下线，需要重新分配，就会批量更新
 *
 * @author: xl
 * @date: 2021/9/7
 **/
@Component
@Slf4j
public class HystrixBatchNotifyHandler implements GatewayRpcServerHandlerService {

    ISerialize iSerialize = SerializeFactory.getInstance().getISerialize(SerializeType.protobuf);


    @Override
    public void execute(RpcMsg rpcMsg, Connection connection) {
        List<PushResponse> responses = iSerialize.deserialize(rpcMsg.getBody(), List.class);
        for (PushResponse response : responses) {
            PullAndPushService.getInstance().updateHystrix(response);
        }


    }
}
