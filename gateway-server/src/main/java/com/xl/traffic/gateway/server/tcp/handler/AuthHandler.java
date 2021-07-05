package com.xl.traffic.gateway.server.tcp.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.thread.ThreadPoolExecutorUtil;
import org.springframework.stereotype.Component;

import java.nio.channels.Channel;

/**
 * gateway鉴权,认证
 *
 * @author: xl
 * @date: 2021/7/5
 **/
@Component
public class AuthHandler implements GatewayServerHandlerService {


    @Override
    public void execute(RpcMsg rpcMsg, Channel channel) {

        //todo 执行业务
        ThreadPoolExecutorUtil.getCommonIOPool().submit(() -> {


        });


    }
}
