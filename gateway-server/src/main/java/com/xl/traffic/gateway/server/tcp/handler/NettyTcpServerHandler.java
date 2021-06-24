package com.xl.traffic.gateway.server.tcp.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


/**
 * @Description: tcp 上行消息 处理器
 * @Author: xl
 * @Date: 2021/6/18
 **/
@ChannelHandler.Sharable
public class NettyTcpServerHandler extends SimpleChannelInboundHandler<RpcMsg> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcMsg rpcMsg) throws Exception {

    }
}
