package com.xl.traffic.gateway.core.mq;


import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import org.apache.commons.lang3.RandomUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class MQProvider {
    public static int threadCnt = Runtime.getRuntime().availableProcessors() * 2;
    private static Map<Integer, MessageQueue<RpcMsg>>
            toRPCMsgQueue = new HashMap<>();
    private static final Map<Integer, MessageQueue<ServerNodeInfo>> retryConnectQueueMap = new HashMap<>();
    private static Random random = new Random();

    static {
        for (int i = 0; i < threadCnt; i++) {
            toRPCMsgQueue.put(i, new DefaultMQ<>());
        }
        for (int i = 0; i < GatewayConstants.RETRY_QUEUE_COUNT; i++) {//cpu*2个队列对应cpu*2个消费线程
            retryConnectQueueMap.put(i, new DefaultMQ<>());
        }
    }


    /**得到重试队列*/
    public static MessageQueue<ServerNodeInfo> getRetryConnectQueue(){
        return retryConnectQueueMap.get(RandomUtils.nextInt(0,  GatewayConstants.RETRY_QUEUE_COUNT));
    }
    /**得到重试队列*/
    public static MessageQueue<ServerNodeInfo> getRetryConnectQueueByIndex(int index){
        return retryConnectQueueMap.get(index);
    }

    /**
     * 随机获得队列
     *
     * @return
     */
    public static MessageQueue<RpcMsg> getToRPCMsgQueueByRandom() {
        int index = random.nextInt(threadCnt);
        return toRPCMsgQueue.get(index);
    }


    /**
     * 根据索引获得队列
     *
     * @return
     */
    public static MessageQueue<RpcMsg> getToRPCMsgQueueByIndex(int index) {
        return toRPCMsgQueue.get(index);
    }


    public static int getToRPCMsgQueueSize() {
        return toRPCMsgQueue.size();
    }
}
