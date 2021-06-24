package com.xl.traffic.gateway.rpc.process;


import com.google.common.util.concurrent.RateLimiter;
import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.rpc.init.CmdProcessInitializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 消监听处理器
 *
 * @author: xl
 * @date: 2021/6/24
 **/
@Slf4j
public class RpcMsgProcess {

    public static RpcMsgProcess getInstance() {
        return RpcMsgProcess.InstanceHolder.instance;
    }


    private static class InstanceHolder {
        private static RpcMsgProcess instance = new RpcMsgProcess();
    }

    /**
     * 服务级别的qps
     */
    private RateLimiter rateLimiter = null;
    /**
     * 全局服务qps
     */
    private int qps;

    public RpcMsgProcess() {
    }

    public void qps(int qps) {
        this.qps = qps;
        if (qps > 0) {
            rateLimiter = RateLimiter.create(qps, 1, TimeUnit.SECONDS);
        }
    }

    public RpcMsg onMessageProcess(RpcMsg rpcMsg) {

        /**针对整个服务做限制qps*/
        if (rateLimiter != null) {
            if (!rateLimiter.tryAcquire()) {
                rpcMsg.setBody("qps limiter out of gauge !!".getBytes());
                return rpcMsg;
            }
        }
        /**调用具体的cmd 处理业务*/
        ICmdProcess cmdProcess = CmdProcessInitializer.getInstance().getCmdProcess(rpcMsg
                .getCmd());
        try {
            byte[] result = cmdProcess.execute(rpcMsg);
            rpcMsg.setBody(result);
        } catch (Exception ex) {
            log.error("onMessageProcess is error:{}", ex);
        }
        return rpcMsg;
    }


}

