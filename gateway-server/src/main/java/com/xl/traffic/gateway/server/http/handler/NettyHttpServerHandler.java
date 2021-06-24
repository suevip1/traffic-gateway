package com.xl.traffic.gateway.server.http.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

@ChannelHandler.Sharable
public class NettyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {



    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        
    }
}
