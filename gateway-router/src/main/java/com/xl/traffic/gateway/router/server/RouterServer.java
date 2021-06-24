package com.xl.traffic.gateway.router.server;

import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.server.AbstractNettyServerInlization;
import com.xl.traffic.gateway.core.server.Server;
import com.xl.traffic.gateway.core.server.acceptor.ChannelAcceptor;
import com.xl.traffic.gateway.core.server.connection.ConnectionInitializer;
import com.xl.traffic.gateway.core.utils.GatewayPortConstants;
import com.xl.traffic.gateway.router.server.initializer.RouterConnectionInitializer;

public class RouterServer extends AbstractNettyServerInlization implements Server {


    public RouterServer() {
        super(AddressUtils.getInnetIp(), GatewayPortConstants.TCP_ROUTER_PORT);
    }

    @Override
    public void start() {
        ssl();
        ConnectionInitializer connectionInitializer = new RouterConnectionInitializer(sslEngineFactory);
        ChannelAcceptor channelAcceptor = new ChannelAcceptor(connectionInitializer);
        init(channelAcceptor);
    }

    @Override
    public void stop() {
        super.stop();
    }
}
