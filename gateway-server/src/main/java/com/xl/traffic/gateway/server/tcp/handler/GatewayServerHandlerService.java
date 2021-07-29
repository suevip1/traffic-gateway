package com.xl.traffic.gateway.server.tcp.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.server.connection.Connection;
import io.netty.channel.Channel;


/**
 * handler接口
 *
 * @author: xl
 * @date: 2021/7/5
 **/
public interface GatewayServerHandlerService {


    /**
     * handler 执行业务
     *
     * @param rpcMsg  消息
     * @param connection 连接
     * @return: void
     * @author: xl
     * @date: 2021/7/5
     **/
    void execute(RpcMsg rpcMsg, Connection connection);


}
