package com.xl.traffic.gateway.server.tcp.handler;

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
public class ServerHandlerInitializer {

    private static class InstanceHolder {
        private static ServerHandlerInitializer instance = new ServerHandlerInitializer();
    }

    public static ServerHandlerInitializer getInstance() {
        return InstanceHolder.instance;
    }

    private ConcurrentMap<Integer, GatewayServerHandlerService> handlerMap = new ConcurrentHashMap<Integer, GatewayServerHandlerService>();

    @Autowired
    LoginHandler loginHandler;

    @Autowired
    LoginOutHandler loginOutHandler;

    /**
     * 获取Handler
     *
     * @param cmd
     * @return
     */
    public GatewayServerHandlerService getHandler(int cmd) {
        return handlerMap.get(cmd);
    }

    @PostConstruct
    private void init() {
        handlerMap.put((int) MsgCMDType.LOGIN_CMD.getType(), loginHandler);
        handlerMap.put((int) MsgCMDType.LOGIN_OUT_CMD.getType(), loginOutHandler);
    }


}
