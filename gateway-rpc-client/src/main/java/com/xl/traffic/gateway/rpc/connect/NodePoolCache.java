
package com.xl.traffic.gateway.rpc.connect;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xl.traffic.gateway.core.enums.LoadBalanceType;
import com.xl.traffic.gateway.core.loadbalance.strategy.RpcLoadBalanceStrategy;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.core.utils.ThreadPoolUtils;
import com.xl.traffic.gateway.rpc.client.RpcClient;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.*;

/**
 * @author xl
 * @date: 2020-12-18
 * @desc: 负责管理各个节点对应的channel集合, 方便对节点尽心中心化管理，统一上线/下线
 */
@Slf4j
public class NodePoolCache {

    /**
     * 节点连接池
     */
    private static Cache<String, CopyOnWriteArrayList<String>> nodePoolMap = Caffeine.newBuilder()
            .maximumSize(GatewayConstants.CONNECT_CACHE_MAX_SIZE)
            .executor(ThreadPoolUtils.getExecutorCaffinePool())
            .build();

    /**
     * 获取节点的channel数量
     *
     * @param node 节点
     * @return: int
     * @author: xl
     * @date: 2021/6/24
     **/
    public static int nodeRpcSize(String node) {
        CopyOnWriteArrayList<String> nodelConnects = nodePoolMap.getIfPresent(node);
        if (!CollectionUtils.isEmpty(nodelConnects)) {
            return nodelConnects.size();
        }
        return 0;
    }

    /**
     * 添加服务
     *
     * @param node   节点
     * @param key    channel连接索引
     * @param client rpc客户端
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    public static void addActionRpcSrv(String node, String key, RpcClient client) {

        synchronized (node.intern()) {
            CopyOnWriteArrayList<String> actionRpcList = getAllNodeRpcSrvListByNode(node);
            if (CollectionUtils.isEmpty(actionRpcList)) {
                actionRpcList = new CopyOnWriteArrayList<>();
            }
            actionRpcList.add(key);
            ConnectionCache.putIfAbsent(key, client);
            nodePoolMap.put(node, actionRpcList);
            show();
        }


    }


    /**
     * 获取所有节点服务信息
     *
     * @param node 节点
     * @return: java.util.concurrent.CopyOnWriteArrayList<java.lang.String>
     * @author: xl
     * @date: 2021/6/24
     **/
    public static CopyOnWriteArrayList<String> getAllNodeRpcSrvListByNode(String node) {
        CopyOnWriteArrayList<String> actionRpcList = nodePoolMap.getIfPresent(node);
        if (CollectionUtils.isEmpty(actionRpcList)) {
            return null;
        }
        return actionRpcList;
    }


    /**
     * 移除服务channel
     *
     * @param group      应用组
     * @param node       应用服务节点
     * @param channelKey channel连接索引
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    public static void removeActionRpcSrv(String group, String node, String channelKey) {
        synchronized (node.intern()) {
            List<String> actionRpcList = getAllNodeRpcSrvListByNode(node);
            if (!CollectionUtils.isEmpty(actionRpcList) && actionRpcList.size() == 1) {
                /**当该节点所有的channel 都挂掉之后，移除节点,移除服务组的对应的节点服务*/
                actionRpcList.remove(channelKey);
                nodePoolMap.invalidate(node);
                GroupNodePoolCache.removeGroupNode(group, node);
            }
            ConnectionCache.remove(channelKey);
            actionRpcList.remove(channelKey);
            show();
        }
    }


    /**
     * 移除节点
     */
    public static void removeAction(String node) {
        List<String> actionRpcList = getAllNodeRpcSrvListByNode(node);
        if (!CollectionUtils.isEmpty(actionRpcList)) {
            for (String connectFlag : actionRpcList) {
                ConnectionCache.remove(connectFlag);
            }
        }
        nodePoolMap.invalidate(node);
    }

    public static void remove(String node) {
        nodePoolMap.invalidate(node);
        show();
    }

    /**
     * 展示连接数量
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    public static void show() {
        log.info("####### 当前节点数量: {},  当前节点连接池信息：{}", nodePoolMap.estimatedSize(), nodePoolMap);


    }
}
