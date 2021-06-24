package com.xl.traffic.gateway.start;


import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.gson.GSONUtil;
import com.xl.traffic.gateway.core.server.Server;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.core.utils.GatewayPortConstants;
import com.xl.traffic.gateway.core.utils.NodelUtil;
import com.xl.traffic.gateway.core.zk.ZkHelp;
import com.xl.traffic.gateway.rpc.cluster.ClusterCenter;
import com.xl.traffic.gateway.rpc.pool.NodePoolManager;
import com.xl.traffic.gateway.server.http.HttpServer;
import com.xl.traffic.gateway.server.rpc.GatewayRpcServer;
import com.xl.traffic.gateway.server.tcp.TcpServer;

public class GatewayServerStart {

    private static class InstanceHolder {
        public static final GatewayServerStart instance = new GatewayServerStart();
    }

    public static GatewayServerStart getInstance() {
        return GatewayServerStart.InstanceHolder.instance;
    }


    public GatewayServerStart() {
        tcpServer = new TcpServer();
        httpServer = new HttpServer();
        gatewayRpcServer = new GatewayRpcServer();
    }

    private Server tcpServer;
    private Server httpServer;
    private Server gatewayRpcServer;

    /**
     * 服务启动
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    public void start() {
        tcpServer.start();
        httpServer.start();
        gatewayRpcServer.start();
        /**注册gateway信息*/
        registerServer();
        /**连接router集群*/
        NodePoolManager.getInstance().initNodePool(GatewayConstants.ROUTER_ZK_ROOT_PATH);
        /**监听router集群*/
        ClusterCenter.getInstance().listenerServerRpc(GatewayConstants.ROUTER_ZK_ROOT_PATH);
        /**连接大业务集群*/
        NodePoolManager.getInstance().initNodePool(GatewayConstants.ROOT_RPC_SERVER_PATH_PREFIX);
        /**监听大业务集群*/
        ClusterCenter.getInstance().listenerServerRpc(GatewayConstants.ROOT_RPC_SERVER_PATH_PREFIX);
    }


    /**
     * 注册rpc服务信息
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    public void registerServer() {
        /**注册服务信息*/
        ZkHelp.getInstance().regInCluster(GatewayConstants.GATEWAY_ZK_ROOT_PATH,
                GSONUtil.toJson(
                        NodelUtil.getInstance().buildServerNodeInfo(
                                GatewayConstants.GATEWAY, GatewayConstants.GATEWAY_GROUP, AddressUtils.getInnetIp(),
                                GatewayPortConstants.TCP_PORT_INNER, GatewayConstants.WEIGHT, -1, -1, GatewayConstants.RPC_POOL_SIZE, "tcp", "")
                ));
    }


    /**
     * 服务停止
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    public void stop() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (tcpServer != null) {
                    tcpServer.stop();
                }
                if (httpServer != null) {
                    httpServer.stop();
                }
                if (gatewayRpcServer != null) {
                    gatewayRpcServer.stop();
                }
            }
        });
    }


}
