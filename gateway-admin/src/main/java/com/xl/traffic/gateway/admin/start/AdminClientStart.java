package com.xl.traffic.gateway.admin.start;

import com.xl.traffic.gateway.admin.config.AdminClientConfig;
import com.xl.traffic.gateway.admin.server.AdminTcpServer;
import com.xl.traffic.gateway.client.server.GatewayClientServer;
import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.gson.GSONUtil;
import com.xl.traffic.gateway.core.server.Server;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.core.utils.GatewayPortConstants;
import com.xl.traffic.gateway.core.utils.NodelUtil;
import com.xl.traffic.gateway.core.zk.ZkHelp;
import com.xl.traffic.gateway.rpc.cluster.ClusterCenter;
import com.xl.traffic.gateway.rpc.pool.NodePoolManager;
import com.xl.traffic.gateway.rpc.process.RpcMsgProcess;

public class AdminClientStart {


    private static class InstanceHolder {
        public static final AdminClientStart instance = new AdminClientStart();
    }

    public static AdminClientStart getInstance() {
        return AdminClientStart.InstanceHolder.instance;
    }


    public AdminClientStart() {
        tcpServer = new AdminTcpServer();
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
        ZkHelp.getInstance().regInCluster(GatewayConstants.ADMIN_ZK_ROOT_PATH,
                GSONUtil.toJson(
                        NodelUtil.getInstance().buildServerNodeInfo(
                                GatewayConstants.ADMIN, GatewayConstants.ADMIN_GROUP, AddressUtils.getInnetIp(),
                                GatewayPortConstants.TCP_ADMIN_PORT,
                                GatewayConstants.WEIGHT, -1, -1, GatewayConstants.RPC_POOL_SIZE, "tcp", "")
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
                if (tcpServer != null) {
                    tcpServer.stop();
                }
            }
        });
    }


}
