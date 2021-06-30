package com.xl.traffic.gateway.core.server.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.context.NettyContext;
import com.xl.traffic.gateway.core.enums.MsgCMDType;
import com.xl.traffic.gateway.core.enums.NettyType;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class NettyReciveHandler extends SimpleChannelInboundHandler<RpcMsg> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMsg cmd) throws Exception {
        //心态数据包直接响应
        if (cmd.getCmd() ==
                (byte) MsgCMDType.HEAT_CMD.getType()) {
            if (NettyContext.currentType().equals(NettyType.client)) {
                //设置值
                ctx.writeAndFlush(cmd);
                return;
            }
            return;
        }
        // 通知执行下一个InboundHandler
        ctx.fireChannelRead(cmd);
    }
}
