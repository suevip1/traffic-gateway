package com.xl.traffic.gateway.rpc.cluster;


import com.github.zkclient.IZkChildListener;
import com.github.zkclient.IZkDataListener;
import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.gson.GSONUtil;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.core.utils.NodelUtil;
import com.xl.traffic.gateway.register.zookeeper.ZkHelp;
import com.xl.traffic.gateway.rpc.pool.NodePoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Zk Cluster管理
 *
 * @author xl
 * @version 2020年11月20日
 */
public class ClusterCenter {


    private Logger log = LoggerFactory.getLogger(ClusterCenter.class);

    private static ZkHelp zkHelp = ZkHelp.getInstance();

    // 内部静态类方式
    private static class InstanceHolder {
        private static ClusterCenter instance = new ClusterCenter();
    }

    public static ClusterCenter getInstance() {
        return InstanceHolder.instance;
    }

    public ClusterCenter() {

    }

    public List<String> serverRpcList = null;


    /**
     * @Description: 监控rpc节点下所有服务节点变化
     * @Param: [zkPath]
     * @return: void
     * @Author: xl
     * @Date: 2021/6/23
     **/
    public void listenerMonitorServerRpc(String zkPath) {
        serverRpcList = zkHelp.getChildren(zkPath);
        log.info("serverRpcList:{}", serverRpcList);
        IZkChildListener listener = new IZkChildListener() {
            public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception {
                // 监听到子节点变化 更新cluster
                log.info("----->>>>> Starting handle children change " + parentPath + "/" + currentChildren + " size=" + currentChildren.size());
                serverRpcList = currentChildren;
                List<ServerNodeInfo> nodeInfos = new ArrayList<>();
                for (String node : currentChildren) {
                    try {
                        /**校验是否有子节点*/
                        List<String> nodeChildDatas = zkHelp.getChildren(zkPath + "/" + node);
                        if (!CollectionUtils.isEmpty(nodeChildDatas)) {
                            /**监听的是大业务集群节点的变化*/
                            //存在子节点
                            for (String nodeChildIp : nodeChildDatas) {
                                /**获取当前节点数据*/
                                nodeInfos.add(NodelUtil.getInstance().getServerNodeInfo(zkPath + "/" + node, nodeChildIp));
                                /**监控当前服务节点的变化*/
                                listenerServerRpcConfig(zkPath + "/" + node, nodeChildIp);
                            }
                        } else {
                            /**监听的是gateway、router、monitor、admin节点的变化*/
                            /**获取当前节点数据*/
                            nodeInfos.add(NodelUtil.getInstance().getServerNodeInfo(zkPath, node));
                            /**监控当前服务节点的变化*/
                            listenerServerRpcConfig(zkPath, node);
                        }
                    } catch (Exception e) {
                        log.error("onNodeDataChange.parseObject", e);
                    }
                }
                NodePoolManager.getInstance().onNodeChange(nodeInfos);
            }
        };
        // 监控节点变更
        zkHelp.subscribeChildChanges(zkPath, listener);
    }


    /**
     * @Description: 监听具体rpc 节点下的具体服务ip配置信息的变化
     * @Param: [zkPath, ip]
     * @return: void
     * @Author: xl
     * @Date: 2021/6/23
     **/
    public void listenerServerRpcConfig(String zkPath, String ip) {
        String listenerPath = zkPath + "/" + ip;
        IZkDataListener listener = new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, byte[] data) throws Exception {
                // 监听到子节点变化 更新cluster
                log.info("----->>>>> Starting handle data change " + "/" + dataPath + " data=" + new String(data));
                String dataStr = new String(data);
                List<ServerNodeInfo> nodeInfos = new ArrayList<>();
                try {
                    ServerNodeInfo nodeInfo = GSONUtil.fromJson(dataStr, ServerNodeInfo.class);
                    nodeInfo.setZkPath(zkPath);
                    if (nodeInfo != null) nodeInfos.add(nodeInfo);
                } catch (Exception e) {
                    log.error("onNodeDataChange.parseObject", e);
                }
                NodePoolManager.getInstance().initRpcPoolSize( nodeInfos);
            }

            @Override
            public void handleDataDeleted(String s) throws Exception {
            }
        };
        // 监控节点变更
        zkHelp.subscribeDataChanges(listenerPath, listener);
    }


}
