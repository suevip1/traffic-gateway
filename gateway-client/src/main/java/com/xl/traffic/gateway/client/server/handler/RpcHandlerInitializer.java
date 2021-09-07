package com.xl.traffic.gateway.client.server.handler;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * handler 初始化
 *
 * @author: xl
 * @date: 2021/7/5
 **/
@Component
public class RpcHandlerInitializer {
    private static class InstanceHolder {
        private static RpcHandlerInitializer instance = new RpcHandlerInitializer();
    }

    public static RpcHandlerInitializer getInstance() {
        return RpcHandlerInitializer.InstanceHolder.instance;
    }

    private ConcurrentMap<Integer, RpcProcesserService> rpcProcesserServiceMap = new ConcurrentHashMap<Integer, RpcProcesserService>();

    /**
     * 获取 RpcProcesserService
     *
     * @param cmd
     * @return
     */
    public RpcProcesserService getRpcProcesserService(int cmd) {
        return rpcProcesserServiceMap.get(cmd);
    }


    /**
     * 注册  RpcProcesserService
     *
     * @param cmd
     * @param rpcProcesserService
     * @return: void
     * @author: xl
     * @date: 2021/9/7
     **/
    public void registerRpcProcesserService(int cmd, RpcProcesserService rpcProcesserService) {
        rpcProcesserServiceMap.put(cmd, rpcProcesserService);
    }

}
