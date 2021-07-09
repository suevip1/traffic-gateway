
package com.xl.traffic.gateway.core.server.handler;

import com.xl.traffic.gateway.core.metrics.MetricsMonitor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author ukuz90
 */
@ChannelHandler.Sharable
public class MonitorBytesHandler extends ChannelDuplexHandler {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            //上报 monitor
            MetricsMonitor.getBytes().addAndGet(((ByteBuf) msg).readableBytes());
        } else if (msg instanceof BinaryWebSocketFrame) {
            MetricsMonitor.getBytes().addAndGet(((BinaryWebSocketFrame) msg).content().readableBytes());
        } else if (msg instanceof TextWebSocketFrame) {
            MetricsMonitor.getBytes().addAndGet(((TextWebSocketFrame) msg).content().readableBytes());
        }
        super.channelRead(ctx, msg);
    }

    //todo 网络出口流量
//    @Override
//    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//        if (msg instanceof ByteBuf) {
//            MetricsMonitor.getResponseBytes(serverName).increment(((ByteBuf) msg).readableBytes());
//        } else if (msg instanceof BinaryWebSocketFrame) {
//            MetricsMonitor.getResponseBytes(serverName).increment(((BinaryWebSocketFrame)msg).content().readableBytes());
//        } else if (msg instanceof TextWebSocketFrame) {
//            MetricsMonitor.getResponseBytes(serverName).increment(((TextWebSocketFrame)msg).content().readableBytes());
//        }
//        super.write(ctx, msg, promise);
//    }

}
