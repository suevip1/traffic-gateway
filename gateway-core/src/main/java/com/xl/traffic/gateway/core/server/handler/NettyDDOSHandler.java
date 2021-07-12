package com.xl.traffic.gateway.core.server.handler;

import com.xl.traffic.gateway.core.helper.DDOSHelper;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author: xl
 * @date: 2021/7/12
 **/
@ChannelHandler.Sharable
public class NettyDDOSHandler extends ChannelDuplexHandler {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ddosDong(ctx, msg);
    }

    /**
     * DDOS逻辑
     * 思路 ：
     * 1 ， 识别来源ip
     * 2 ， 在一段时间窗口内 ， 超出频次限制拉进黑名单
     * 3 ， 强制断开该来源ip的连接
     * 4 ， 黑名单智能安全校验
     * 5 ， 一段时间内不活跃的连接 进行主动关闭)
     *
     * @param ctx
     * @return: void
     * @author: xl
     * @date: 2021/7/12
     **/
    private void ddosDong(ChannelHandlerContext ctx, Object msg) {
        /**获取来源ip*/
        String remoteAddress = ctx.channel().remoteAddress().toString();
        if (DDOSHelper.ddosIpLimit(remoteAddress)) {
            /**强制断开连接*/
            ctx.channel().close();
            return;
        }
        ctx.fireChannelRead(msg);
    }


}
