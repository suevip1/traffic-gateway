package com.xl.traffic.gateway.server.rpc.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Description: 下行rpc消息处理器，通过长链推送
 * @Author: xl
 * @Date: 2021/6/22
 **/
public class GatewayNettyRpcHandler extends SimpleChannelInboundHandler<RpcMsg> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcMsg rpcMsg) throws Exception {




    }
}
