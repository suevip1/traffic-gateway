package com.xl.traffic.gateway.router.server.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import io.netty.channel.Channel;

/**
 * handler接口
 *
 * @author: xl
 * @date: 2021/7/5
 **/
public interface RouterServerHandlerService {


    /**
     * handler 执行业务
     *
     * @param rpcMsg  消息
     * @param channel 连接
     * @return: void
     * @author: xl
     * @date: 2021/7/5
     **/
    void execute(RpcMsg rpcMsg, Channel channel);


}
