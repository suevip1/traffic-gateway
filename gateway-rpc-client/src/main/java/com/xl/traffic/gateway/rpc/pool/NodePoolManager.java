package com.xl.traffic.gateway.rpc.pool;


import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.core.enums.LoadBalanceType;
import com.xl.traffic.gateway.core.exception.RPCException;
import com.xl.traffic.gateway.core.gson.GSONUtil;
import com.xl.traffic.gateway.core.loadbalance.RpcLoadBalance;
import com.xl.traffic.gateway.core.loadbalance.strategy.RpcLoadBalanceStrategy;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.core.utils.NodelUtil;
import com.xl.traffic.gateway.register.zookeeper.ZkHelp;
import com.xl.traffic.gateway.rpc.client.RpcClient;
import com.xl.traffic.gateway.rpc.cluster.ClusterCenter;
import com.xl.traffic.gateway.rpc.connect.ConnectionCache;
import com.xl.traffic.gateway.rpc.connect.WeightNodelCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.xl.traffic.gateway.rpc.connect.WeightNodelCache.loadBalance;

/**
 * @author xuliang
 * @date 2019年3月18日 下午3:16:41
 * <p>
 * 链接zookeeper,建立连接池
 */
public class NodePoolManager {


    // 内部静态类方式
    private static class InstanceHolder {
        private static NodePoolManager instance = new NodePoolManager();
    }

    public static NodePoolManager getInstance() {
        return InstanceHolder.instance;
    }


    private static final Logger logger = LoggerFactory.getLogger(NodePoolManager.class);

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private ZkHelp zkHelp = ZkHelp.getInstance();


    /**
     * @Description: 初始化连接池
     * @Param: [zkPath]
     * @return: void
     * @Author: xl
     * @Date: 2021/6/23
     **/
    public void initNodePool(String zkPath) {
        /**获取节点列表*/
        List<String> nodeDatas = zkHelp.getChildren(zkPath);
        List<ServerNodeInfo> nodeInfos = new ArrayList<>();
        for (String nodeIp : nodeDatas) {
            try {
                /**校验是否有子节点*/
                List<String> nodeChildDatas = zkHelp.getChildren(GatewayConstants.ROOT_RPC_SERVER_PATH_PREFIX + nodeIp);
                if (!CollectionUtils.isEmpty(nodeChildDatas)) {
                    //存在子节点
                    for (String nodeChildIp : nodeChildDatas) {
                        /**获取当前节点数据*/
                        nodeInfos.add(NodelUtil.getInstance().getServerNodeInfo(zkPath + nodeIp, nodeChildIp));
                        /**监控当前服务节点的变化*/
                        ClusterCenter.getInstance().listenerServerRpcConfig(zkPath + nodeIp, nodeChildIp);
                    }
                } else {
                    /**获取当前节点数据*/
                    nodeInfos.add(NodelUtil.getInstance().getServerNodeInfo(zkPath, nodeIp));
                    /**监控当前服务节点的变化*/
                    ClusterCenter.getInstance().listenerServerRpcConfig(zkPath, nodeIp);
                }
            } catch (Exception e) {
                logger.error("onNodeDataChange.parseObject", e);
            }
        }
        /**初始化连接池及权重值变化*/
        initPoolAndWeight(zkPath
                , nodeInfos);
    }

    /**
     * 节点变更通知
     */
    public void onNodeChange(String zkPath, List<ServerNodeInfo> nodeDatas) {
        try {
            lock.writeLock().lock();
            logger.info("onNodeDataChange->" + nodeDatas.size() + "=" + GSONUtil.toJson(nodeDatas));
            /**初始化连接并赋予权重值*/
            initPoolAndWeight(zkPath, nodeDatas);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 初始化连接并赋予权重值
     */
    public void initPoolAndWeight(String zkPath, List<ServerNodeInfo> nodeDatas) {
        for (ServerNodeInfo nodeInfo : nodeDatas) {
            /**step1: 建立连接*/
            ConnectionPoolFactory.getInstance().zkSyncRpcServer(zkPath, nodeInfo);
            /**step2: 添加服务负载均衡*/
            WeightNodelCache.addGroupRpcLoadBalance(nodeInfo.getGroup(), RpcLoadBalanceStrategy.getInstance().getRpcLoadBalance(LoadBalanceType.WEIGHT));
            /**step3: 添加服务负载节点信息*/
            WeightNodelCache.addGroupNodel(nodeInfo.getGroup(), nodeInfo);
        }
        /**step4: 初始化服务节点权重*/
        RpcLoadBalanceStrategy.getInstance().getRpcLoadBalance(LoadBalanceType.WEIGHT).initWeight();
    }


    /**
     * 初始化连接并赋予权重值
     */
    public void initRpcPoolSize(String zkPath, List<ServerNodeInfo> nodeDatas) {
        for (ServerNodeInfo nodeInfo : nodeDatas) {
            /**step1: 建立连接*/
            ConnectionPoolFactory.getInstance().zkSyncRpcServer(zkPath, nodeInfo);
        }
    }


    /**
     * 根据选择服务器,支持权重
     */
    public RpcClient chooseRpcClient(String group) {
        try {
            lock.readLock().lock();
            RpcLoadBalance rpcLoadBalance =WeightNodelCache.loadBalance(group);
            String channelKey = rpcLoadBalance.loadBalance();
            if (StringUtils.isEmpty(channelKey)) {
                logger.info(">>>>>>> channel 不存在，请检查服务是否发生异常！！！");
                throw new RPCException(" channel 不存在，请检查调用服务是否发生异常！！！");
            }
            logger.info(">>>>>>> current choose server node key :{} ", channelKey);
            return ConnectionCache.get(channelKey);

        } finally {
            lock.readLock().unlock();
        }

    }


}
