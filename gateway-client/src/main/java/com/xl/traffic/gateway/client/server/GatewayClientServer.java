package com.xl.traffic.gateway.client.server;

import com.xl.traffic.gateway.client.server.initializer.GatewayRpcConnectionInitializer;
import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.server.AbstractNettyServerInlization;
import com.xl.traffic.gateway.core.server.Server;
import com.xl.traffic.gateway.core.server.acceptor.ChannelAcceptor;
import com.xl.traffic.gateway.core.server.connection.ConnectionInitializer;

public class GatewayClientServer extends AbstractNettyServerInlization implements Server {

    public GatewayClientServer(int port) {
        super(AddressUtils.getInnetIp(), port);
    }

    @Override
    public void start() {
        ssl();
        ConnectionInitializer connectionInitializer = new GatewayRpcConnectionInitializer(sslEngineFactory);
        ChannelAcceptor channelAcceptor = new ChannelAcceptor(connectionInitializer);
        init(channelAcceptor);
    }

    @Override
    public void stop() {
        super.stop();
    }
}
