package com.xl.traffic.gateway.core.utils;

import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.gson.GSONUtil;
import com.xl.traffic.gateway.register.zookeeper.ZkHelp;

public class NodelUtil {


    // 内部静态类方式
    private static class InstanceHolder {
        private static NodelUtil instance = new NodelUtil();
    }

    public static NodelUtil getInstance() {
        return InstanceHolder.instance;
    }


    ZkHelp zkHelp = ZkHelp.getInstance();
    
    /**
     * 获取节点数据
     *
     * @param zkPath zk服务路径
     * @param nodeIp 节点ip
     * @return: com.xl.traffic.gateway.common.node.ServerNodeInfo
     * @author: xl
     * @date: 2021/6/24
     **/
    public ServerNodeInfo getServerNodeInfo(String zkPath, String nodeIp) {
        /**获取当前节点数据*/
        String nodeData = zkHelp.getValue(zkPath +
                "/" + nodeIp);
        ServerNodeInfo nodeInfo = GSONUtil.fromJson(nodeData, ServerNodeInfo.class);
        return nodeInfo;
    }

    /**
     * 构造服务信息
     *
     * @param appName     应用名称
     * @param group       组
     * @param ip          服务ip
     * @param port        服务端口号
     * @param weight      服务权重
     * @param qps         服务全局qps
     * @param cmdQps      服务接口qps
     * @param rpcPoolSize 服务连接池大小
     * @param singnalType 服务类型  tcp/http
     * @param zip         服务压缩类型
     * @return: com.xl.traffic.gateway.common.node.ServerNodeInfo
     * @author: xl
     * @date: 2021/6/24
     **/
    public ServerNodeInfo buildServerNodeInfo(String appName, String group, String ip,
                                              int port, int weight, int qps, int cmdQps, int rpcPoolSize,
                                              String singnalType, String zip) {
        ServerNodeInfo serverNodeInfo =
                ServerNodeInfo.builder()
                        .appName(appName)
                        .group(group)
                        .ip(ip)
                        .port(port)
                        .weight(weight)
                        .qps(qps)
                        .cmdQps(cmdQps)
                        .rpcPoolSize(rpcPoolSize)
                        .signalType(singnalType)
                        .zip(zip)
                        .build();
        return serverNodeInfo;
    }


}
