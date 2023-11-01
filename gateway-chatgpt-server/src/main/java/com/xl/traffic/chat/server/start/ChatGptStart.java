package com.xl.traffic.chat.server.start;

import com.xl.traffic.chat.server.server.ChatGptServer;
import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.gson.GSONUtil;
import com.xl.traffic.gateway.core.server.Server;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.core.utils.GatewayPortConstants;
import com.xl.traffic.gateway.core.utils.NodelUtil;
import com.xl.traffic.gateway.register.zookeeper.ZkHelp;
import com.xl.traffic.gateway.rpc.cluster.ClusterCenter;
import com.xl.traffic.gateway.rpc.pool.NodePoolManager;

/**
 * @author xuliang
 * @version 1.0
 * @project traffic-gateway
 * @description
 * @date 2023/10/26 14:36:35
 */
public class ChatGptStart {


    private static class InstanceHolder {
        public static final ChatGptStart instance = new ChatGptStart();
    }

    public static ChatGptStart getInstance() {
        return ChatGptStart.InstanceHolder.instance;
    }


    private Server chatGptServer;

    public ChatGptStart() {
        chatGptServer = new ChatGptServer();
    }


    /**
     * @Description: 服务启动
     * @Param:
     * @return: void
     * @Author: xl
     * @Date: 2021/6/23
     **/
    public void start() {
        chatGptServer.start();
        /**注册chatgpt集群*/
        registerServer();
        /**连接 gateway server集群*/
        NodePoolManager.getInstance().connectNodePool(GatewayConstants.GATEWAY_ZK_ROOT_PATH);
        /**监听 gateway server集群*/
        ClusterCenter.getInstance().listenerMonitorServerRpc(GatewayConstants.GATEWAY_ZK_ROOT_PATH);
        /**连接大业务集群*/
        NodePoolManager.getInstance().connectNodePool(GatewayConstants.ROOT_RPC_SERVER_PATH_PREFIX);
        /**监听大业务集群*/
        ClusterCenter.getInstance().listenerMonitorServerRpc(GatewayConstants.ROOT_RPC_SERVER_PATH_PREFIX);

    }


    /**
     * @Description: 注册rpc服务信息
     * @Param: []
     * @return: void
     * @Author: xl
     * @Date: 2021/6/24
     **/
    public void registerServer() {
        /**注册服务信息*/
        ZkHelp.getInstance().regInCluster(GatewayConstants.CHAT_GPT_ZK_ROOT_PATH,
                GSONUtil.toJson(NodelUtil.getInstance().buildServerNodeInfo(
                        GatewayConstants.CHAT_GPT, GatewayConstants.CHAT_GPT_GROUP, AddressUtils.getInnetIp(),
                        GatewayPortConstants.TCP_CHATGPT_PORT, GatewayConstants.WEIGHT, -1,
                        -1, GatewayConstants.RPC_POOL_SIZE, GatewayConstants.TCP, "")
                ));
    }

    /**
     * @Description: 服务停止
     * @Param:
     * @return: void
     * @Author: xl
     * @Date: 2021/6/23
     **/
    public void stop() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (chatGptServer != null) {
                    chatGptServer.stop();
                }
            }
        });
    }









}
