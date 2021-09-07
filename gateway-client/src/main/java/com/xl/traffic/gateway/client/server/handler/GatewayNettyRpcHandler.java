package com.xl.traffic.gateway.client.server.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.enums.SerializeType;
import com.xl.traffic.gateway.core.serialize.ISerialize;
import com.xl.traffic.gateway.core.serialize.SerializeFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Description: gatewayClient 处理器
 * @Author: xl
 * @Date: 2021/6/23
 **/
@ChannelHandler.Sharable
public class GatewayNettyRpcHandler extends SimpleChannelInboundHandler<RpcMsg> {



    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcMsg rpcMsg) throws Exception {


        


    }
}
