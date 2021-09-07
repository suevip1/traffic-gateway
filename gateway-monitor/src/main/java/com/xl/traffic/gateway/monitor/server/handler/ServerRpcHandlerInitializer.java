package com.xl.traffic.gateway.monitor.server.handler;

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
        return InstanceHolder.instance;
    }

    private ConcurrentMap<Integer, MonitorServerHandlerService> handlerMap = new ConcurrentHashMap<Integer, MonitorServerHandlerService>();

    @Autowired
    RegisterMonitorDataHandler registerMonitorDataHandler;


    /**
     * 获取Handler
     *
     * @param cmd
     * @return
     */
    public MonitorServerHandlerService getHandler(int cmd) {
        return handlerMap.get(cmd);
    }

    @PostConstruct
    private void init() {
        handlerMap.put((int) MsgCMDType.REGISTER_MONITOR_TASK.getType(), registerMonitorDataHandler);

    }



}
