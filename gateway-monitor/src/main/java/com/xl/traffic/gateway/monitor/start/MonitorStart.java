package com.xl.traffic.gateway.monitor.start;

import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.gson.GSONUtil;
import com.xl.traffic.gateway.core.server.Server;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.core.utils.GatewayPortConstants;
import com.xl.traffic.gateway.core.utils.NodelUtil;
import com.xl.traffic.gateway.monitor.server.MonitorServer;
import com.xl.traffic.gateway.register.zookeeper.ZkHelp;
import com.xl.traffic.gateway.rpc.cluster.ClusterCenter;
import com.xl.traffic.gateway.rpc.pool.NodePoolManager;

public class MonitorStart {

    private static class InstanceHolder {
        public static final MonitorStart instance = new MonitorStart();
    }

    public static MonitorStart getInstance() {
        return MonitorStart.InstanceHolder.instance;
    }


    public MonitorStart() {
        monitorServer = new MonitorServer();
    }

    private Server monitorServer;


    /**
     * @Description: 服务启动
     * @Param:
     * @return: void
     * @Author: xl
     * @Date: 2021/6/23
     **/
    public void start() {
        monitorServer.start();
        /**注册router集群*/
        registerServer();
        /**连接 gateway server集群*/
        NodePoolManager.getInstance().initNodePool(GatewayConstants.GATEWAY_ZK_ROOT_PATH);
        /**监听 gateway server集群*/
        ClusterCenter.getInstance().listenerServerRpc(GatewayConstants.GATEWAY_ZK_ROOT_PATH);


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
        ZkHelp.getInstance().regInCluster(GatewayConstants.MONITOR_ZK_ROOT_PATH,
                GSONUtil.toJson(NodelUtil.getInstance().buildServerNodeInfo(
                        GatewayConstants.MONITOR, GatewayConstants.MONITOR_GROUP, AddressUtils.getInnetIp(),
                        GatewayPortConstants.TCP_MONITOR_PORT, GatewayConstants.WEIGHT, -1,
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
                if (monitorServer != null) {
                    monitorServer.stop();
                }
            }
        });
    }


}
