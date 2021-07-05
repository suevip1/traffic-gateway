package com.xl.traffic.gateway.producer;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.mq.MQProvider;

import java.time.Duration;

public class RpcMsgProducer {
    private void onNewRpcMsg(RpcMsg rpcMsg) {
        MQProvider.getToRPCMsgQueueByRandom().push(rpcMsg, Duration.ofMillis(100));
    }
}
