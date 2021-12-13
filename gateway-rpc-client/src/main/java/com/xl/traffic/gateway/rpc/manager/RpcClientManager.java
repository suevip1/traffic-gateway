package com.xl.traffic.gateway.rpc.manager;


import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.mq.MQProvider;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.rpc.client.RpcClient;
import com.xl.traffic.gateway.rpc.connect.ConnectionCache;
import com.xl.traffic.gateway.rpc.connect.GroupNodePoolCache;
import com.xl.traffic.gateway.rpc.connect.NodePoolCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;


/**
 * 连接池客户端管理
 *
 * @author xl
 * @date 2020-11-23
 */
public class RpcClientManager {

    private static Logger log = LoggerFactory.getLogger(RpcClientManager.class);

    public RpcClientManager() {
    }

    private static class InstanceHolder {
        public static final RpcClientManager instance = new RpcClientManager();
    }

    public static RpcClientManager getInstance() {
        return InstanceHolder.instance;
    }


    /**
     * 连接
     */
    public void connect(ServerNodeInfo nodeInfo, int index) {

        String rpcServer = nodeInfo.getIp();
        int rpcPort = nodeInfo.getPort();
        boolean isConnected = false;
        int rpcRetryTimes = GatewayConstants.RETRY_QUEUE_COUNT;
        int i = 0;
        final String localIp = AddressUtils.getInnetIp();
        while (!isConnected) {
            String key = rpcServer + GatewayConstants.SEQ + rpcPort + GatewayConstants.SEQ + index;
            nodeInfo.setId(key);//设置本次连接唯一标识
            i++;
            if (i > rpcRetryTimes) {
                log.info("###### 连接失败，appName={},key:{}  到达重试次数上线 retryCount:{}  添加服务监控队列中...",nodeInfo.getAppName(), key, i, rpcRetryTimes);
                /**添加监控队列*/
                MQProvider.getRetryConnectQueue().push(nodeInfo, Duration.ofMillis(1000));
                break;
            }
            log.info("###### 开始对 {} 进行第 {}/{} 次连接...", key, i, rpcRetryTimes);
            try {
                RpcClient client0 = ConnectionCache.get(key);
                log.info("###### 开始重新连接IM...   appName={}, key={},    imServerIp={},	 localIp={},    client0={},    clientMap.get(key))={},   clientMap.size()={}", nodeInfo.getAppName(),key, rpcServer, localIp, client0, ConnectionCache.get(key), ConnectionCache.rpcPoolSize());
                if (client0 == null) {
                    synchronized (key.intern()) {
                        RpcClient client = new RpcClient(nodeInfo, index, key);   //服务端IP， 端口， 连接池索引
                        if (client.connection()) {
                            //添加服务组对应的节点信息
                            GroupNodePoolCache.addGroupNode(nodeInfo.getGroup(), nodeInfo.getIp());
                            NodePoolCache.addActionRpcSrv(nodeInfo.getIp(), key, client);
                            isConnected = true;
                            log.info("@@@@RPC Server 连接成功！ appName={},key={},     imServerIp={},	 localIp={},    clientMap.get(key)={},   clientMap.size()={}",nodeInfo.getAppName(), key, rpcServer, localIp, ConnectionCache.get(key), ConnectionCache.rpcPoolSize());
                        }
                    }
                } else {
                    log.info("map中 {}   连接已存在,停止连接  client0={} !!!!!!!!!!!!!!", key, client0);
                    break;
                }
            } catch (Exception e) {
                NodePoolCache.removeActionRpcSrv(nodeInfo
                        .getGroup(), nodeInfo.getIp(), key);
                log.error("重连失败! 继续尝试... appName={}, key={}, e.toString()={}",nodeInfo.getAppName(), key, e.toString());
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                //nothing to do
            }
        }
    }
}
