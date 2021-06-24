package com.xl.traffic.gateway.router.start;

import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.gson.GSONUtil;
import com.xl.traffic.gateway.core.server.Server;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.core.utils.GatewayPortConstants;
import com.xl.traffic.gateway.core.utils.NodelUtil;
import com.xl.traffic.gateway.core.zk.ZkHelp;
import com.xl.traffic.gateway.router.server.RouterServer;
import com.xl.traffic.gateway.rpc.cluster.ClusterCenter;
import com.xl.traffic.gateway.rpc.pool.NodePoolManager;

/**
 * @Description: router服务
 * @Author: xl
 * @Date: 2021/6/23
 **/
public class RouterStart {

    private static class InstanceHolder {
        public static final RouterStart instance = new RouterStart();
    }

    public static RouterStart getInstance() {
        return RouterStart.InstanceHolder.instance;
    }

    private Server routerServer;

    public RouterStart() {
        routerServer = new RouterServer();
    }


    /**
     * @Description: 服务启动
     * @Param:
     * @return: void
     * @Author: xl
     * @Date: 2021/6/23
     **/
    public void start() {
        routerServer.start();
        /**注册router集群*/
        registerServer();
        /**连接 gateway server集群*/
        NodePoolManager.getInstance().initNodePool(GatewayConstants.GATEWAY_ZK_ROOT_PATH);
        /**监听 gateway server集群*/
        ClusterCenter.getInstance().listenerServerRpc(GatewayConstants.GATEWAY_ZK_ROOT_PATH);
        /**连接大业务集群*/
        NodePoolManager.getInstance().initNodePool(GatewayConstants.ROOT_RPC_SERVER_PATH_PREFIX);
        /**监听大业务集群*/
        ClusterCenter.getInstance().listenerServerRpc(GatewayConstants.ROOT_RPC_SERVER_PATH_PREFIX);

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
        ZkHelp.getInstance().regInCluster(GatewayConstants.ROUTER_ZK_ROOT_PATH,
                GSONUtil.toJson(NodelUtil.getInstance().buildServerNodeInfo(
                        GatewayConstants.ROUTER, GatewayConstants.ROUTER_GROUP, AddressUtils.getInnetIp(),
                        GatewayPortConstants.TCP_ROUTER_PORT, GatewayConstants.WEIGHT, -1,
                        -1, GatewayConstants.RPC_POOL_SIZE, "tcp", "")
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
                if (routerServer != null) {
                    routerServer.stop();
                }
            }
        });
    }

}
