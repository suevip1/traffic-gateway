package com.xl.traffic.gateway.core.rpc;

import com.xl.traffic.gateway.core.mq.MQProvider;
import com.xl.traffic.gateway.core.mq.MessageQueue;
import com.xl.traffic.gateway.common.msg.RpcMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RpcMsgConsumer {

    private static final Logger logger = LoggerFactory.getLogger(RpcMsgConsumer.class);

    private static class InstanceHolder {
        public static final RpcMsgConsumer instance = new RpcMsgConsumer();
    }

    public static RpcMsgConsumer getInstance() {
        return InstanceHolder.instance;
    }


    public void start() {

        ExecutorService detectThreadPool = Executors.newFixedThreadPool(MQProvider.threadCnt);
        for (int i = 0; i < MQProvider.threadCnt; i++) {
            detectThreadPool.execute(new RpcMsgSender(
                    i % MQProvider.threadCnt));
        }
        logger.info("AsyncLoadCacheConsumer Detect Async Page Queue Start！！Thread Count:{}", MQProvider.threadCnt);
    }


    private class RpcMsgSender implements Runnable {


        private int index;
        private final Duration timeout;


        private MessageQueue<RpcMsg> toRPCMsgQueue = null;

        private RpcMsgSender(int i) {
            index = i;
            timeout = Duration.ofMillis(100);
            toRPCMsgQueue = MQProvider.getToRPCMsgQueueByIndex(index);
        }


        @Override
        public void run() {

            while (true) {
                try {
                    RpcMsg msg = toRPCMsgQueue.pop(timeout);

                    if (logger.isDebugEnabled()) {
                        logger.debug("RpcMsgSender RpcMsgSender pop msg: {}", msg);
                    }

                    //todo 路由转发 获取对应服务的通信类型 支持 grpc，http协议
//                    switch (msg.getType()) {
//                        case RPC_PUB2RPC:
//                            sendPublishMsg(msg);
//                            break;
//                        case CLUSTER_BC:
//                            broadcastMsg(msg);
//                            break;
//                        default:
//                            break;
//                    }
                } catch (Exception ignore) {

                }
            }


        }
    }


}
