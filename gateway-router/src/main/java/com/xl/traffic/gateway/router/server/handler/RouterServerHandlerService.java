package com.xl.traffic.gateway.router.server.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;

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
     * @param connection 连接
     * @return: void
     * @author: xl
     * @date: 2021/7/5
     **/
    void execute(RpcMsg rpcMsg, Connection connection);


}
