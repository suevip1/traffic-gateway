
package com.xl.traffic.gateway.core.server.handler;

import com.xl.traffic.gateway.core.metrics.MetricsMonitor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author: xl
 * @date: 2021/7/12
 **/
@ChannelHandler.Sharable
public class MonitorBytesHandler extends ChannelDuplexHandler {

    /**
     * 入口流量统计
     *
     * @param ctx
     * @param msg
     * @return: void
     * @author: xl
     * @date: 2021/8/13
     **/
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            //上报 monitor
            MetricsMonitor.getRequestBytes().addAndGet(((ByteBuf) msg).readableBytes());
        } else if (msg instanceof BinaryWebSocketFrame) {
            MetricsMonitor.getRequestBytes().addAndGet(((BinaryWebSocketFrame) msg).content().readableBytes());
        } else if (msg instanceof TextWebSocketFrame) {
            MetricsMonitor.getRequestBytes().addAndGet(((TextWebSocketFrame) msg).content().readableBytes());
        }
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
        if (msg instanceof ByteBuf) {
            MetricsMonitor.getResponseBytes().addAndGet(((ByteBuf) msg).readableBytes());
        } else if (msg instanceof BinaryWebSocketFrame) {
            MetricsMonitor.getResponseBytes().addAndGet(((BinaryWebSocketFrame) msg).content().readableBytes());
        } else if (msg instanceof TextWebSocketFrame) {
            MetricsMonitor.getResponseBytes().addAndGet(((TextWebSocketFrame) msg).content().readableBytes());
        }
        super.write(ctx, msg, promise);
    }

}
