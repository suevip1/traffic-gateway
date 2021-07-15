package com.xl.traffic.gateway.producer;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.consumer.RpcMsgConsumer;
import com.xl.traffic.gateway.core.mq.MQProvider;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

public class RpcMsgProducer {

    private static class InstanceHolder {
        public static final RpcMsgProducer instance = new RpcMsgProducer();
    }

    public static RpcMsgProducer getInstance() {
        return InstanceHolder.instance;
    }


    public void onNewRpcMsg(RpcMsg rpcMsg) {
        MQProvider.getToRPCMsgQueueByRandom().push(rpcMsg, Duration.ofMillis(100));
    }
}
