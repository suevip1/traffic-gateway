package com.xl.traffic.gateway.core.rpc;

import com.xl.traffic.gateway.core.mq.MQProvider;
import com.xl.traffic.gateway.common.msg.RpcMsg;

import java.time.Duration;

public class RpcMsgProcessor {

    private void onNewRpcMsg(RpcMsg rpcMsg) {
        MQProvider.getToRPCMsgQueueByRandom().push(rpcMsg, Duration.ofMillis(100));
    }

}
