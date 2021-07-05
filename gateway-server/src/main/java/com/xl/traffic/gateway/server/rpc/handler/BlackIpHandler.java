package com.xl.traffic.gateway.server.rpc.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.thread.ThreadPoolExecutorUtil;
import com.xl.traffic.gateway.server.tcp.handler.GatewayServerHandlerService;
import org.springframework.stereotype.Component;

import java.nio.channels.Channel;

/**
 * 黑名单设置
 *
 * @author: xl
 * @date: 2021/7/5
 **/
@Component
public class BlackIpHandler implements GatewayServerHandlerService {


    @Override
    public void execute(RpcMsg rpcMsg, Channel channel) {

        //todo 执行业务
        ThreadPoolExecutorUtil.getCommonIOPool().submit(() -> {


        });


    }
}
