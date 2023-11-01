package com.xl.traffic.gateway.rpc.pool;

import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.gson.GSONUtil;
import com.xl.traffic.gateway.rpc.connect.ConnectionCache;
import com.xl.traffic.gateway.rpc.manager.RpcClientManager;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xl
 * @date: 2020-12-18
 * @desc: 连接池调度管理工厂
 */
@Slf4j
public class ConnectionPoolFactory {


    private static class InstanceHolder {
        public static final ConnectionPoolFactory instance
                = new ConnectionPoolFactory();
    }

    public ConnectionPoolFactory() {

    }

    public static ConnectionPoolFactory getInstance() {
        return InstanceHolder.instance;
    }


    /**
     * 初始化zk RPC连接
     *
     * @param
     */
    public void zkSyncRpcServer(ServerNodeInfo nodeInfo) {

        /**配置的连接池队列 一定要比缓存的连接池队列数量要大，只支持高峰时期的扩容，不支持缩容*/
        int rpcPoolSize = nodeInfo.getRpcPoolSize();
        int cacheRpcpoolSize = (int) ConnectionCache.rpcPoolSize();
        int initIndex = 0;
        if (cacheRpcpoolSize > 0) {
            initIndex = cacheRpcpoolSize;
        }
        final String localIp = AddressUtils.getInnetIp();
        try {
            log.info("###### 开始连接 rpc长连接服务...	localIp={},  nodeInfo={},  zkPath={}", localIp,
                    GSONUtil.toJson(nodeInfo)
                    , nodeInfo.getZkPath());
            //创建连接池
            for (int index = initIndex; index < rpcPoolSize; index++) {
                int finalIndex = index;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        RpcClientManager.getInstance().connect(nodeInfo, finalIndex);
                    }
                }).start();
            }
        } catch (Exception e) {
            log.error("RPC create error! 服务创建失败! host=" + nodeInfo.getIp() + "	" + e);
        }

    }


}
