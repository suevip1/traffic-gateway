package com.xl.traffic.gateway.rpc.init;

import com.xl.traffic.gateway.rpc.process.ICmdProcess;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 初始化 cmd process
 *
 * @author xl
 * @version 2017年10月16日
 */
public class CmdProcessInitializer {

    private static class InstanceHolder {
        private static CmdProcessInitializer instance = new CmdProcessInitializer();
    }

    public static CmdProcessInitializer getInstance() {
        return InstanceHolder.instance;
    }

    private ConcurrentMap<Integer, ICmdProcess> handlerMap = new ConcurrentHashMap<Integer, ICmdProcess>();


    /**
     * 获取 cmd process
     *
     * @param cmd
     * @return
     */
    public ICmdProcess getCmdProcess(int cmd) {
        return handlerMap.get(cmd);
    }

    /**
     * 注册 cmd process
     */
    private void registerCmdProcess(int cmd, ICmdProcess cmdProcess) {
        getInstance().handlerMap.put(cmd, cmdProcess);

    }

}