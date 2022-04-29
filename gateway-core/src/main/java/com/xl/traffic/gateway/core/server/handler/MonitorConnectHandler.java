
package com.xl.traffic.gateway.core.server.handler;

import com.xl.traffic.gateway.core.metrics.MetricsMonitor;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * @author: xl
 * @date: 2021/7/12
 **/
@ChannelHandler.Sharable
public class MonitorConnectHandler extends ChannelDuplexHandler {

    /**
     * 连接数统计
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        MetricsMonitor.getConnectNum().incrementAndGet();
        super.channelActive(ctx);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        /**连接数-1*/
        MetricsMonitor.getConnectNum().decrementAndGet();
        super.channelInactive(ctx);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        /**出现异常情况，连接数-1*/
        MetricsMonitor.getConnectNum().decrementAndGet();
    }
}
