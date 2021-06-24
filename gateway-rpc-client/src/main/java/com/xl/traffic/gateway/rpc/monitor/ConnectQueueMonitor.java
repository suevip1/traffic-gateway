package com.xl.traffic.gateway.rpc.monitor;


import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.core.mq.MQProvider;
import com.xl.traffic.gateway.core.mq.MessageQueue;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.rpc.manager.RpcClientManager;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 连接状态监控
 *
 * @author xl
 * @desc: 当出现掉线，网络抖动等情况，
 * 会触发重连，重连失败时(如果docker 容器部署时，会有一种服务被重复拉取的情况，
 * 导致监听端口存在，服务不能用的情况)，会加入到该队列，由队列监控
 */
@Slf4j
public class ConnectQueueMonitor {


    private static class InstanceHolder {
        public static final ConnectQueueMonitor instance = new ConnectQueueMonitor();
    }

    public static ConnectQueueMonitor getInstance() {
        return InstanceHolder.instance;
    }


    private ExecutorService msgSenderExecutor;

    public ConnectQueueMonitor() {
    }

    public void start() {
        msgSenderExecutor = Executors.newFixedThreadPool(GatewayConstants.RETRY_QUEUE_COUNT);
        for (int i = 0; i < GatewayConstants.RETRY_QUEUE_COUNT; i++) {
            msgSenderExecutor.execute(new ConnectConsumerWorker(i));
        }
    }

    private class ConnectConsumerWorker implements Runnable {

        private final Duration timeout = Duration.ofMillis(100);
        private final MessageQueue<ServerNodeInfo> retryConnectQueue;

        public ConnectConsumerWorker(int index) {
            this.retryConnectQueue = MQProvider.getRetryConnectQueueByIndex(index);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (null != retryConnectQueue &&
                            retryConnectQueue.size() > 0) {
                        ServerNodeInfo msg = retryConnectQueue.pop(timeout);
                        RpcClientManager.getInstance().connect(msg, msg.getRpcServerIndex());
                    }
                    /**1000ms执行一次*/
                    Thread.sleep(1000);
                } catch (Exception ignore) {
                    log.error("retryConnectQueue.pop", ignore);
                }
            }
        }
    }


}
