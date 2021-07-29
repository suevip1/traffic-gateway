package com.xl.traffic.gateway.rpc.connect;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.core.loadbalance.RpcLoadBalance;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.core.utils.ThreadPoolUtils;

/**
 * group负载均衡服务 缓存信息
 *
 * @author: xl
 * @date: 2021/6/24
 **/
public class WeightNodelCache {

    /**
     * 应用组-应用服务集合
     */
    private static Cache<String, RpcLoadBalance> groupNodeServers = Caffeine.newBuilder()
            .maximumSize(GatewayConstants.CONNECT_CACHE_MAX_SIZE)
            .executor(ThreadPoolUtils.getExecutorCaffinePool())
            .build();

    /**
     * 添加应用组的服务信息
     *
     * @param group          应用组
     * @param serverNodeInfo 应用服务信息
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    public static void addGroupNodel(String group, ServerNodeInfo serverNodeInfo) {
        RpcLoadBalance rpcLoadBalance = loadBalance(group);
        if (null != rpcLoadBalance) {
            //校验是否存在服务节点
            if (rpcLoadBalance.groupExistNodel(serverNodeInfo.getIp())) {
                return;
            }
            rpcLoadBalance.addNode(serverNodeInfo);
            rpcLoadBalance.group(group);
        }
    }


    /**
     * 添加应用组的负载均衡
     *
     * @param group          应用组
     * @param rpcLoadBalance 负载均衡服务
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    public static void addGroupRpcLoadBalance(String group, RpcLoadBalance rpcLoadBalance) {
        if (loadBalance(group) != null) {
            return;
        }
        rpcLoadBalance.group(group);
        groupNodeServers.put(group, rpcLoadBalance);
    }

    /**
     * 移除应用组的服务节点
     *
     * @param group 应用组
     * @param node  服务节点
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    public static void removeGroupNodel(String group, String node) {
        RpcLoadBalance rpcLoadBalance = loadBalance(group);
        if (null != rpcLoadBalance) {
            rpcLoadBalance.removeNode(node);
        }
    }

    /**
     * 移除负载均衡服务
     *
     * @param group
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    public static void removeGroupLoadBalance(String group) {
        groupNodeServers.invalidate(group);
    }

    /**
     * 获取负载均衡服务
     *
     * @param group 应用组
     * @return: com.xl.traffic.gateway.core.loadbalance.RpcLoadBalance
     * @author: xl
     * @date: 2021/6/24
     **/
    public static RpcLoadBalance loadBalance(String group) {
        RpcLoadBalance rpcLoadBalance = groupNodeServers.getIfPresent(group);
        return rpcLoadBalance;
    }


}
