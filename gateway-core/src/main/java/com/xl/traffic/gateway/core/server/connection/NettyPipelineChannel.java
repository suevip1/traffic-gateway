package com.xl.traffic.gateway.core.server.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;

public class NettyPipelineChannel implements ConnectionFacade {


    private final Channel channel;

    public NettyPipelineChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void addHandler(Object connectionHandler) {
        channel.pipeline().addLast((ChannelHandler) connectionHandler);
    }

}
