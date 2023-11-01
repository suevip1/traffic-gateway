package com.xl.traffic.gateway.start;


import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.consumer.RpcMsgConsumer;
import com.xl.traffic.gateway.core.gson.GSONUtil;
import com.xl.traffic.gateway.core.server.Server;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.core.utils.GatewayPortConstants;
import com.xl.traffic.gateway.core.utils.NodelUtil;
import com.xl.traffic.gateway.hystrix.notify.DowngrateActionNotify;
import com.xl.traffic.gateway.hystrix.service.PullAndPushService;
import com.xl.traffic.gateway.monitor.MonitorReport;
import com.xl.traffic.gateway.register.zookeeper.ZkHelp;
import com.xl.traffic.gateway.rpc.cluster.ClusterCenter;
import com.xl.traffic.gateway.rpc.pool.NodePoolManager;
import com.xl.traffic.gateway.server.http.HttpServer;
import com.xl.traffic.gateway.server.rpc.GatewayRpcServer;
import com.xl.traffic.gateway.server.tcp.TcpServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        Thread start = new Thread(new Runnable() {
            @Override
            public void run() {
                tcpServer.start();
                httpServer.start();
                gatewayRpcServer.start();
                /**注册gateway信息*/
                registerServer();
                /**连接其它gateway服务集群*/
                NodePoolManager.getInstance().connectNodePool(GatewayConstants.GATEWAY_ZK_ROOT_PATH);
                /**监听其它gateway集群*/
                ClusterCenter.getInstance().listenerMonitorServerRpc(GatewayConstants.GATEWAY_ZK_ROOT_PATH);
                /**连接monitor集群*/
                NodePoolManager.getInstance().connectNodePool(GatewayConstants.MONITOR_ZK_ROOT_PATH);
                /**监听monitor集群*/
                ClusterCenter.getInstance().listenerMonitorServerRpc(GatewayConstants.MONITOR_ZK_ROOT_PATH);
                /**连接router集群*/
                NodePoolManager.getInstance().connectNodePool(GatewayConstants.ROUTER_ZK_ROOT_PATH);
                /**监听router集群*/
                ClusterCenter.getInstance().listenerMonitorServerRpc(GatewayConstants.ROUTER_ZK_ROOT_PATH);
                /**连接admin集群*/
                NodePoolManager.getInstance().connectNodePool(GatewayConstants.ADMIN_ZK_ROOT_PATH);
                /**监听admin集群*/
                ClusterCenter.getInstance().listenerMonitorServerRpc(GatewayConstants.ADMIN_ZK_ROOT_PATH);
                /**连接大业务集群{eg:chat,room,groupchat,task,api,chatgpt}*/
                NodePoolManager.getInstance().connectNodePool(GatewayConstants.ROOT_IM_RPC_SERVER_PATH_PREFIX);
                /**监听大业务集群{eg:chat,room,groupchat,task,api,chatgpt}*/
                ClusterCenter.getInstance().listenerMonitorServerRpc(GatewayConstants.ROOT_IM_RPC_SERVER_PATH_PREFIX);
                /**启动消费者*/
                RpcMsgConsumer.getInstance().start();
                /**初始化任务*/
                initTask();
            }
        });
        start.setName("gateway-server-start-thread");
        start.setDaemon(true);
        start.start();
    }

    /**
     * 初始化任务
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/9/6
     **/
    public void initTask() {
        /**step1:注册申报gateway服务指标健康信息*/
        MonitorReport.registerReportMonitorData();
        /**step2:初始化降级,同步降级策略*/
        PullAndPushService.getInstance().syncAllHystrixPointStrategyFromAdminServer();
        /**step3:添加降级监听器*/
        addDowngrateEventListener();
    }


    /**
     * 添加降级监听器
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/6/30
     **/
    public void addDowngrateEventListener() {
        DowngrateActionNotify.addDowngradeActionListener((point, downgradeActionType, time) -> {
            log.info("point:{} is downgraded at {} type:{}", point, time, downgradeActionType);
        });
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
        /**构建服务信息*/
        ServerNodeInfo nodeInfo = NodelUtil.getInstance().buildServerNodeInfo(
                GatewayConstants.GATEWAY,
                GatewayConstants.GATEWAY_GROUP,
                AddressUtils.getInnetIp(),
                GatewayPortConstants.TCP_PORT_INNER,
                GatewayConstants.WEIGHT,
                -1,
                -1,
                GatewayConstants.RPC_POOL_SIZE,
                GatewayConstants.TCP,
                "");
        /**注册服务信息*/
        ZkHelp.getInstance().regInCluster(GatewayConstants.GATEWAY_ZK_ROOT_PATH,
                GSONUtil.toJson(nodeInfo));
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
