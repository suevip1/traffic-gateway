package com.xl.traffic.gateway.monitor.server.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;

import java.nio.channels.Channel;

/**
 * handler接口
 *
 * @author: xl
 * @date: 2021/7/5
 **/
public interface MonitorServerHandlerService {


    /**
     * handler 执行业务
     *
     * @param rpcMsg  消息
     * @param channel channel通道
     * @return: void
     * @author: xl
     * @date: 2021/7/5
     **/
    void execute(RpcMsg rpcMsg, Channel channel);


}
