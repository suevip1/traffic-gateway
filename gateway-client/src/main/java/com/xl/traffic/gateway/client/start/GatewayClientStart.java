package com.xl.traffic.gateway.client.start;

import com.xl.traffic.gateway.client.config.GatewayClientConfig;
import com.xl.traffic.gateway.client.server.GatewayClientServer;
import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.gson.GSONUtil;
import com.xl.traffic.gateway.core.server.Server;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.core.utils.GatewayPortConstants;
import com.xl.traffic.gateway.core.utils.NodelUtil;
import com.xl.traffic.gateway.register.zookeeper.ZkHelp;
import com.xl.traffic.gateway.rpc.cluster.ClusterCenter;
import com.xl.traffic.gateway.rpc.pool.NodePoolManager;
import com.xl.traffic.gateway.rpc.process.RpcMsgProcess;

public class GatewayClientStart {


    private static class InstanceHolder {
        public static final GatewayClientStart instance = new GatewayClientStart();
    }

    public static GatewayClientStart getInstance() {
        return GatewayClientStart.InstanceHolder.instance;
    }


    private GatewayClientConfig gatewayClientConfig;

    public GatewayClientStart(GatewayClientConfig gatewayClientConfig) {
        this.gatewayClientConfig = gatewayClientConfig;
    }

    public GatewayClientStart() {
        tcpServer = new GatewayClientServer(gatewayClientConfig.getTcpPort());
    }

    private Server tcpServer;

    /**
     * @Description: 服务开启
     * @Param:
     * @return: void
     * @Author: xl
     * @Date: 2021/6/23
     **/
    public void start() {
        tcpServer.start();
        /**注册服务信息*/
        registerServer();
        /**设置服务的qps*/
        RpcMsgProcess.getInstance().qps(gatewayClientConfig.getQps());
        /**连接router集群*/
        NodePoolManager.getInstance().initNodePool(GatewayConstants.ROUTER_ZK_ROOT_PATH);
        /**监听router集群*/
        ClusterCenter.getInstance().listenerServerRpc(GatewayConstants.ROUTER_ZK_ROOT_PATH);
        /**连接gateway集群*/
        NodePoolManager.getInstance().initNodePool(GatewayConstants.GATEWAY_ZK_ROOT_PATH);
        /**监听gateway集群*/
        ClusterCenter.getInstance().listenerServerRpc(GatewayConstants.GATEWAY_ZK_ROOT_PATH);
    }

    /**
     * @Description: 注册服务信息
     * @Param: []
     * @return: void
     * @Author: xl
     * @Date: 2021/6/24
     **/
    public void registerServer() {
        /**注册服务信息*/
        ZkHelp.getInstance().regInCluster(
                GatewayConstants.ROOT_RPC_SERVER_PATH_PREFIX
                        + gatewayClientConfig.getAppName(),
                GSONUtil.toJson(NodelUtil.getInstance().buildServerNodeInfo(
                        gatewayClientConfig.getAppName(),
                        gatewayClientConfig.getGroup(),
                        AddressUtils.getInnetIp(),
                        gatewayClientConfig.getTcpPort(),
                        gatewayClientConfig.getWeight(),
                        gatewayClientConfig.getQps(),
                        gatewayClientConfig.getCmdQps(),
                        gatewayClientConfig.getRpcPoolSize(),
                        gatewayClientConfig.getSignalType(),
                        gatewayClientConfig.getZip()
                )));
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
                if (tcpServer != null) {
                    tcpServer.stop();
                }
            }
        });
    }


}
