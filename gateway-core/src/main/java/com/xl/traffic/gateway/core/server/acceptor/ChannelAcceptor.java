package com.xl.traffic.gateway.core.server.acceptor;

import com.xl.traffic.gateway.core.server.connection.ConnectionFacade;
import com.xl.traffic.gateway.core.server.connection.ConnectionInitializer;
import com.xl.traffic.gateway.core.server.connection.NettyPipelineChannel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * channel的接收器，对connection初始化，并对connection做适配
 * {@link ConnectionInitializer} connection 初始化，添加handler
 */
public class ChannelAcceptor extends ChannelInitializer<SocketChannel> {

    private final ConnectionInitializer connectionInitializer;

    public ChannelAcceptor(final ConnectionInitializer connectionInitializer) {
        this.connectionInitializer = connectionInitializer;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ConnectionFacade connectionFacade = new NettyPipelineChannel(socketChannel);
        connectionInitializer.initConnection(connectionFacade);
    }
}
