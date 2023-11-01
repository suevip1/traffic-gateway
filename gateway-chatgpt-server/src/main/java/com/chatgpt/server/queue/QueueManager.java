package com.chatgpt.server.queue;

import com.chatgpt.server.message.ImMessage;
import com.chatgpt.server.queue.receiver.DisruptorReceiverQueue;
import com.chatgpt.server.queue.send.DisruptorSendQueue;
import com.chatgpt.server.utils.HashCodeUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xuliang
 * @version 1.0
 * @project traffic-gateway
 * @description
 * @date 2023/10/26 15:11:56
 */
public class QueueManager {


    // TODO: 2023/10/26
    private static int inThreadCount = 16;
    private static int outThreadCount = 16;


    private static class InstanceHolder {
        public static final QueueManager instance = new QueueManager();
    }
    public static QueueManager getInstance() {
        return QueueManager.InstanceHolder.instance;
    }

    public static final Map<Integer, DisruptorReceiverQueue> disruptorReceiverQueueMap = new HashMap<>();
    public static final Map<Integer, DisruptorSendQueue> disruptorSendQueueMap = new HashMap<>();


    public QueueManager() {
    }

    public void initReceiverQueue() {
        for (int i = 0; i < inThreadCount; i++) {
            disruptorReceiverQueueMap.put(i,
                    new DisruptorReceiverQueue());
        }
    }

    public void initSendQueue() {
        for (int i = 0; i < outThreadCount; i++) {
            disruptorSendQueueMap.put(i,
                    new DisruptorSendQueue());
        }
    }

    /**
     * 得到与index相匹配的队列
     *
     * @param index
     * @return
     */
    public static DisruptorReceiverQueue getDisruptorReceiverQueueByIndex(int index) {
        return disruptorReceiverQueueMap.get(index);
    }

    /**
     * 得到与index相匹配的队列
     *
     * @param index
     * @return
     */
    public static DisruptorSendQueue getDisruptorSendQueueByIndex(int index) {
        return disruptorSendQueueMap.get(index);
    }

    /**
     * 得到与key 取模的队列
     *
     * @param key
     * @return
     */
    public static DisruptorReceiverQueue getDisruptorReceiverQueueByIndex(String key) {
        int index = HashCodeUtils.getHashCode(key) % inThreadCount;
        return disruptorReceiverQueueMap.get(index);
    }


    /**
     * 得到与key 取模的队列
     *
     * @param key
     * @return
     */
    public static DisruptorSendQueue getDisruptorSendQueueByIndex(String key) {
        int index = HashCodeUtils.getHashCode(key) % outThreadCount;
        return disruptorSendQueueMap.get(index);
    }

    public static void pushInMessage(ImMessage msg) {
        if (null != msg) {
            getDisruptorReceiverQueueByIndex(msg.getFromUserId()).produceData(msg);
        }
    }

    public static void pushOutMessage(ImMessage msg) {
        if (null != msg) {
            getDisruptorSendQueueByIndex(msg.getFromUserId()).produceData(msg);
        }
    }


}
