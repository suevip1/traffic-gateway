package com.xl.traffic.gateway.core.server;

import com.xl.traffic.gateway.core.context.NettyContext;
import com.xl.traffic.gateway.core.enums.NettyType;
import com.xl.traffic.gateway.core.helper.GatewayConfigHelper;
import com.xl.traffic.gateway.core.server.acceptor.ChannelAcceptor;
import com.xl.traffic.gateway.core.server.ssl.SslEngineFactory;
import com.xl.traffic.gateway.core.server.ssl.impl.DefaultSslEngineFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;


public abstract class AbstractNettyServerInlization {

    private final Logger logger = LoggerFactory.getLogger(AbstractNettyServerInlization.class);
    private EventLoopGroup workerGroup;
    private EventLoopGroup bossGroup;
    private String ip;
    private int port;
    public SslEngineFactory sslEngineFactory;

    public AbstractNettyServerInlization(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * ssl认证处理
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    protected void ssl() {
        try {
            if (sslEngineFactory == null) {
                sslEngineFactory = new DefaultSslEngineFactory();
                sslEngineFactory.init(GatewayConfigHelper.getInstance().getGateWayConfig().getSslConfig());
            }
        } catch (Exception ex) {
            logger.error("ssl init error:{}", ex);
        }
    }


    /**
     * 初始化netty服务
     *
     * @param channelAcceptor
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    protected void init(ChannelAcceptor channelAcceptor) {
        NettyContext.nettyType = NettyType.server;
        bossGroup = new NioEventLoopGroup(2);
        workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);// 默认cpu线程*2
        ServerBootstrap b = new ServerBootstrap();
        // BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50
        b.option(ChannelOption.SO_BACKLOG, 1024);
        // 是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）如果在两小时内没有数据的通信时，TCP会自动发送一个活动探测数据报文
        b.option(ChannelOption.SO_KEEPALIVE, true);
        // 用于启用或关闭Nagle算法。如果要求高实时性，有数据发送时就马上发送，就将该选项设置为true关闭Nagle算法；如果要减少发送次数减少网络交互，就设置为false等累积一定大小后再发送。默认为false。
        b.option(ChannelOption.TCP_NODELAY, false);
        // 缓冲区大小
        b.option(ChannelOption.SO_RCVBUF, 256 * 1024);
        b.option(ChannelOption.SO_SNDBUF, 256 * 1024);
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                // .handler(new LoggingHandler(LogLevel.INFO)) //日记
                .childHandler(channelAcceptor);
        ChannelFuture channelFuture = b.bind(new InetSocketAddress(ip, port));
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info("NettyServer Started Succeeded on {}:{}, registry is complete, waiting for client connect...", ip, port);
                } else {
                    logger.error("NettyServer Started Failed on {}:{}, registry is incomplete {}", ip, port, future.cause());
                }
            }
        });
    }

    /**
     * 关闭netty 服务
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    protected void stop() {
        try {
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            logger.info("NettyServer Stop Succeeded on {}:{}", ip, port);
        } catch (Exception ex) {
            logger.error("server close is error:{}", ex);
        }
    }


}
