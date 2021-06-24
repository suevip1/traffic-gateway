package com.xl.traffic.gateway.rpc.connect;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.core.utils.ThreadPoolUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 业务组服务连接池
 *
 * @author: xl
 * @date: 2021/6/24
 **/
@Slf4j
public class GroupNodePoolCache {

    private static Cache<String, CopyOnWriteArrayList<String>> groupNodelPoolCacheMap =
            Caffeine.newBuilder()
                    .maximumSize(GatewayConstants.CONNECT_CACHE_MAX_SIZE)
                    .executor(ThreadPoolUtils.getExecutorCaffinePool())
                    .build();

    /**
     * 添加组对应的节点服务
     *
     * @param group 应用组
     * @param nodel 应用服务节点
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    public static void addGroupNode(String group, String nodel) {
        synchronized (group.intern()) {
            CopyOnWriteArrayList<String> actionRpcList = getGroupNodes(group);
            if (CollectionUtils.isEmpty(actionRpcList)) {
                actionRpcList = new CopyOnWriteArrayList<>();
            }
            actionRpcList.add(nodel);
            groupNodelPoolCacheMap.put(group, actionRpcList);
            show();
        }
    }


    /**
     * 删除组对应的节点服务
     *
     * @param group 应用组
     * @param nodel 应用服务节点
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    public static void removeGroupNode(String group, String nodel) {

        synchronized (group.intern()) {
            List<String> actionRpcList = getGroupNodes(group);
            if (!CollectionUtils.isEmpty(actionRpcList) && actionRpcList.size() == 1) {
                /**当该节点所有的nodel 都挂掉之后，移除应用组，应用组负载均衡服务*/
                actionRpcList.remove(group);
                groupNodelPoolCacheMap.invalidate(group);
                WeightNodelCache.removeGroupLoadBalance(group);
            }
            actionRpcList.remove(nodel);
            WeightNodelCache.removeGroupNodel(group,nodel);
            show();
        }

    }

    /**
     * 获取组的节点数量
     *
     * @param group 应用组
     * @return: int  返回该应用组的服务数量
     * @author: xl
     * @date: 2021/6/24
     **/
    public static int getGroupNodeSize(String group) {
        CopyOnWriteArrayList<String> nodelConnects = groupNodelPoolCacheMap.getIfPresent(group);
        if (!CollectionUtils.isEmpty(nodelConnects)) {
            return nodelConnects.size();
        }
        return 0;
    }

    /**
     * 获取组的节点集合
     *
     * @param group 应用组
     * @return: java.util.concurrent.CopyOnWriteArrayList<java.lang.String>
     * @author: xl
     * @date: 2021/6/24
     **/
    public static CopyOnWriteArrayList<String> getGroupNodes(String group) {

        CopyOnWriteArrayList<String> groupNodels = groupNodelPoolCacheMap.getIfPresent(group);
        if (!CollectionUtils.isEmpty(groupNodels)) {
            return groupNodels;
        }

        return null;
    }

    /**
     * 显示数据
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    private static void show() {
        log.info("####### 当前组数量: {},  当前组的服务信息：{}", groupNodelPoolCacheMap.estimatedSize(), groupNodelPoolCacheMap);
    }

}
