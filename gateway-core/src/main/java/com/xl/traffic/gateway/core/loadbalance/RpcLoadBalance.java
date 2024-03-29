package com.xl.traffic.gateway.core.loadbalance;

import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.core.enums.LoadBalanceType;

/**
 * @Description: 负载均衡接口
 * @Author: xl
 * @Date: 2021/6/23
 **/
public interface RpcLoadBalance {

    LoadBalanceType getType();


    default void group(String group) {
    }




    /**
     * @Description: 取模获取服务节点
     * @Param: [nodeInfo]
     * @return: void
     * @Author: xl
     * @Date: 2021/6/23
     **/
    default ServerNodeInfo getServerNodeInfoByModelKey(String modelKey) {
        return null;
    }

    ;


    /**
     * @Description: 添加服务节点
     * @Param: [nodeInfo]
     * @return: void
     * @Author: xl
     * @Date: 2021/6/23
     **/
    default void addNode(ServerNodeInfo nodeInfo) {
    }

    ;

    /**
     * @Description: 移除服务节点
     * @Param: [node]
     * @return: void
     * @Author: xl
     * @Date: 2021/6/23
     **/
    default void removeNode(String nodeInfo) {
    }

    ;

    /**
     * @Description: 获取服务节点
     * @Param: []
     * @return: java.lang.String
     * @Author: xl
     * @Date: 2021/6/23
     **/
    String loadBalance();


    /**
     * @Description: 取模获取服务节点
     * @Param: []
     * @return: java.lang.String
     * @Author: xl
     * @Date: 2021/6/23
     **/
    String modelLoadBalance(String modelkey);

    /**
     * @Description: 根据ip获取服务节点
     * @Param: []
     * @return: java.lang.String
     * @Author: xl
     * @Date: 2021/6/23
     **/
    default String loadBalance(String p) {
        return "";
    }

    /**
     * @Description: 初始化服务节点
     * @Param:
     * @return:
     * @Author: xl
     * @Date: 2021/6/23
     **/
    default void initWeight() {
    }

    /**
     * @Description: 是否存在服务节点
     * @Param:
     * @return:
     * @Author: xl
     * @Date: 2021/6/23
     **/
    default boolean groupExistNodel(String nodel) {
        return false;
    }





}
