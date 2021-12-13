package com.xl.traffic.gateway.server.rpc.handler;

import com.xl.traffic.gateway.core.enums.MsgCMDType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * handler 初始化
 *
 * @author: xl
 * @date: 2021/7/5
 **/
@Component
public class ServerRpcHandlerInitializer {


    private static class InstanceHolder {
        private static ServerRpcHandlerInitializer instance = new ServerRpcHandlerInitializer();
    }

    public static ServerRpcHandlerInitializer getInstance() {
        return ServerRpcHandlerInitializer.InstanceHolder.instance;
    }

    private ConcurrentMap<Integer, GatewayRpcServerHandlerService> handlerMap = new ConcurrentHashMap<Integer, GatewayRpcServerHandlerService>();


    @Autowired
    BlackIpHandler blackIpHandler;

    @Autowired
    HystrixNotifyHandler hystrixNotifyHandler;

    @Autowired
    PullGatewayHealthDataHandler pullGatewayHealthDataHandler;

    /**
     * 获取Handler
     *
     * @param cmd
     * @return
     */
    public GatewayRpcServerHandlerService getHandler(int cmd) {
        return handlerMap.get(cmd);
    }


    @PostConstruct
    private void init() {

        handlerMap.put((int) MsgCMDType.BLACK_IP_CMD.getType(), blackIpHandler);
        handlerMap.put((int) MsgCMDType.HYSTRIX_NOTIFY.getType(), hystrixNotifyHandler);
        handlerMap.put((int) MsgCMDType.PULL_GATEWAY_HEALTH_DATA_CMD.getType(), pullGatewayHealthDataHandler);

    }


}
