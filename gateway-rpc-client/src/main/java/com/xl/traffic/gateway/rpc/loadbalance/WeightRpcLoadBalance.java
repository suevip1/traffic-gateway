package com.xl.traffic.gateway.rpc.loadbalance;


import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.core.enums.LoadBalanceType;
import com.xl.traffic.gateway.core.exception.RPCException;
import com.xl.traffic.gateway.core.hash.HashCodeUtils;
import com.xl.traffic.gateway.core.loadbalance.RpcLoadBalance;
import com.xl.traffic.gateway.rpc.connect.NodePoolCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description:基于权重的负载均衡算法
 * @Author: xl
 * @Date: 2021/6/23
 **/
@Component("weight-balance")
public class WeightRpcLoadBalance implements RpcLoadBalance {
    private static Logger logger = LoggerFactory.getLogger(WeightRpcLoadBalance.class);

    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

    static {
        statisPrintLoadBalanceSrv();
    }

    private String group;
    private static List<ServerNodeInfo> nodeInfos = new CopyOnWriteArrayList<>();


    private short[] indexMap;//下标为weight,值为nodeInfos对应的节点index,提升选择性能
    private volatile int weightIndex = -1;
    private int weightSum;
    private Random random = new Random();

    /**
     * 根据权重获取节点
     */
    public String chooseNodeChannel() {
        if (nodeInfos.size() == 1) return getChannelKey(nodeInfos.get(0).getIp());
        if (nodeInfos.size() == 0) return null;
        return getChannelKey(nodeInfos.get(indexMap[nextIndex()]).getIp());
    }

    @Override
    public ServerNodeInfo getServerNodeInfoByModelKey(String modelKey) {
        int hashCode = HashCodeUtils.getHashCode(modelKey);
        int index=hashCode%nodeInfos.size();
        return nodeInfos.get(index);
    }

    /**
     * 从当前节点的连接池中当中随机取出一个链接
     */
    public String getChannelKey(String node) {
        List<String> channelKeys = NodePoolCache.getAllNodeRpcSrvListByNode(node);
        if (CollectionUtils.isEmpty(channelKeys)) {
            /**很有可能当前节点服务全部挂掉*/
            throw new RPCException("######### 当前节点 node：" + node + " 服务不可用！！！");
        }
        //todo 可以进行优化，随机就可以啦，不需要for啦，不然每次也会增加不必要的耗时
        int size = NodePoolCache.nodeRpcSize(node);
        int randomIndex = random.nextInt(size);
        int index = 0;
        for (String channelKey : channelKeys) {
            if (index == randomIndex) {
                return channelKey;
            }
            index++;
        }
        return null;
    }

    /**
     * 20s实时打印负载服务
     */
    public static void statisPrintLoadBalanceSrv() {

        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                for (ServerNodeInfo nodeInfo : nodeInfos) {
                    logger.info("@@@@@@ rpcLoadBalance group:{} node:{}",
                            (nodeInfo.getIp() + ":" + nodeInfo.getPort()));
                }
            }
        }, 0, 20 * 1000, TimeUnit.MILLISECONDS);
    }


    /**
     * 获取节点索引index
     * 加锁,并发有安全问题
     */
    private synchronized int nextIndex() {
        weightIndex++;
        if (weightIndex >= weightSum) weightIndex = 0;
        return weightIndex;
    }

    @Override
    public void group(String group) {
        this.group = group;
    }

    /**
     * 刷新权重映射
     */
    @Override
    public void initWeight() {
        weightSum = 0;

        for (ServerNodeInfo nodeInfo : nodeInfos) weightSum += nodeInfo.getWeight();
        //权重值总和
        indexMap = new short[weightSum];

        //索引
        short index = 0;
        //偏移量
        int offset = 0;

        for (ServerNodeInfo nodeInfo : nodeInfos) {
            for (int i = 0; i < nodeInfo.getWeight(); i++) {
                indexMap[i + offset] = index;
            }
            offset += nodeInfo.getWeight();
            index++;
        }
    }

    @Override
    public String loadBalance() {
        return chooseNodeChannel();
    }


    @Override
    public String modelLoadBalance(String modelkey) {
        ServerNodeInfo serverNodeInfoByModelKey = getServerNodeInfoByModelKey(modelkey);
        return getChannelKey(serverNodeInfoByModelKey.getIp());
    }

    @Override
    public String loadBalance(String ip) {
        ServerNodeInfo node = nodeInfos.stream().filter(nodeInfo -> ip.equals(nodeInfo.getIp())).findAny().get();
        if (node != null) {
            return getChannelKey(node.getIp());
        }
        return null;
    }

    @Override
    public void addNode(ServerNodeInfo nodeInfo) {
        if (!nodeInfos.contains(nodeInfo)) {
            nodeInfos.add(nodeInfo);
        }
    }

    /**
     * 移除掉对应ip
     */
    @Override
    public void removeNode(String nodel) {
        nodeInfos = nodeInfos.stream()
                .filter(node -> !node.getIp().equals(nodel))
                .collect(Collectors.toList());
    }

    @Override
    public boolean groupExistNodel(String nodel) {
        boolean existNode = nodeInfos.stream().anyMatch(nodeInfo -> nodeInfo.getIp().equals(nodel));
        return existNode;
    }

    @Override
    public LoadBalanceType getType() {
        return LoadBalanceType.WEIGHT;
    }

}
