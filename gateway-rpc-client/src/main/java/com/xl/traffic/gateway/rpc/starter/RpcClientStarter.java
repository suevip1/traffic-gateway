package com.xl.traffic.gateway.rpc.starter;

import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.core.context.NettyContext;
import com.xl.traffic.gateway.core.enums.NettyType;
import com.xl.traffic.gateway.core.protocol.MessageDecoder;
import com.xl.traffic.gateway.core.protocol.MessageEncoder;
import com.xl.traffic.gateway.core.server.handler.NettyReciveHandler;
import com.xl.traffic.gateway.core.zip.Zip;
import com.xl.traffic.gateway.rpc.handler.KeepaliveHandler;
import com.xl.traffic.gateway.rpc.handler.RpcClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class RpcClientStarter {


    // 内部静态类方式
    private static class InstanceHolder {
        private static RpcClientStarter instance = new RpcClientStarter();
    }

    public static RpcClientStarter getInstance() {
        return InstanceHolder.instance;
    }


    // 连接配置,需要再独立成配置类
    private static final int connTimeout = 18 * 1000;

    private static final boolean soKeepalive = true;

    private static final boolean soReuseaddr = true;

    private static final boolean tcpNodelay = false;

    private static final int soRcvbuf = 1024 * 256;

    private static final int soSndbuf = 1024 * 256;

    private byte zip, ver;//请求节点的配置

    private SslContext sslContext;

    private Channel channel;
    // TODO 考虑改成静态,所有连接公用同一个线程池
    private static EventLoopGroup bossGroup = new NioEventLoopGroup();


    public RpcClientStarter() {
    }

    public void setSslContext(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    public ChannelFuture connect(ServerNodeInfo nodeInfo) {

        NettyContext.nettyType = NettyType.client;
        if (nodeInfo != null) {
            this.zip = Zip.getInt(nodeInfo.getZip());
        }
        try {
//            bossGroup = new NioEventLoopGroup(zip == 0 ? 1 : 1);//有压缩增加线程数...待定
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connTimeout);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, soKeepalive);
            bootstrap.option(ChannelOption.SO_REUSEADDR, soReuseaddr);
            bootstrap.option(ChannelOption.TCP_NODELAY, tcpNodelay);
            bootstrap.option(ChannelOption.SO_RCVBUF, soRcvbuf);
            bootstrap.option(ChannelOption.SO_SNDBUF, soSndbuf);

            bootstrap.group(bossGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {

                    ChannelPipeline pipeline = ch.pipeline();
                    if (sslContext != null) {
                        /**处理ssl 认证 ，有需求的可以添加上*/
                        pipeline.addLast(sslContext.newHandler(ch.alloc()));
                    }
                    pipeline.addLast(new MessageEncoder());// tcp消息编码
                    pipeline.addLast(new MessageDecoder());// tcp消息解码
                    pipeline.addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));
                    pipeline.addLast(new KeepaliveHandler());//心跳
                    pipeline.addLast(new NettyReciveHandler());
                    pipeline.addLast(new RpcClientHandler());

                    // 以"$_"作为分隔符
                    /*
                     * ChannelPipeline pipeline = ch.pipeline(); pipeline.addLast("encoder", new
                     * StringEncoder(CharsetUtil.UTF_8)); String s = "$_"; ByteBuf byteBuf =
                     * Unpooled.copiedBuffer(s.getBytes()); pipeline.addLast(new
                     * DelimiterBasedFrameDecoder(Integer.MAX_VALUE,byteBuf)); pipeline.addLast(new
                     * StringDecoder()); pipeline.addLast(new MyHeartSocket());
                     */

                }
            });
            // 发起连接操作
            ChannelFuture channelFuture = bootstrap.connect(nodeInfo.getIp(),
                    nodeInfo.getPort()).awaitUninterruptibly();// .sync();

            return channelFuture;

            // 等待监听端口关闭
            // channel.closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();

        } finally {

        }
        return null;
    }

    public void close() {
        if (channel != null)
            channel.close();
        if (bossGroup != null)
            bossGroup.shutdownGracefully();

    }


}
