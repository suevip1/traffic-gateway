
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
public class MonitorQpsHandler extends ChannelDuplexHandler {


    /**
     * 进口流量统计
     *
     * @param ctx
     * @param msg
     * @return: void
     * @author: xl
     * @date: 2021/8/13
     **/
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MetricsMonitor.getRequestQps().incrementAndGet();
        super.channelRead(ctx, msg);
    }

    /**
     * 出口流量统计
     *
     * @param ctx
     * @param msg
     * @param promise
     * @return: void
     * @author: xl
     * @date: 2021/8/13
     **/
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        MetricsMonitor.getResponseQps().incrementAndGet();
        super.channelRead(ctx, msg);
    }
}
