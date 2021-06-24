package com.xl.traffic.gateway.server.rpc;

import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.server.AbstractNettyServerInlization;
import com.xl.traffic.gateway.core.server.Server;
import com.xl.traffic.gateway.core.server.acceptor.ChannelAcceptor;
import com.xl.traffic.gateway.core.server.connection.ConnectionInitializer;
import com.xl.traffic.gateway.core.utils.GatewayPortConstants;
import com.xl.traffic.gateway.server.rpc.initializer.GatewayRpcConnectionInitializer;

public class GatewayRpcServer extends AbstractNettyServerInlization implements Server {

    public GatewayRpcServer() {
        super(AddressUtils.getInnetIp(), GatewayPortConstants.TCP_PORT_INNER);
    }

    @Override
    public void start() {
        super.ssl();
        ConnectionInitializer connectionInitializer = new GatewayRpcConnectionInitializer(sslEngineFactory);
        ChannelAcceptor channelAcceptor = new ChannelAcceptor(connectionInitializer);
        init(channelAcceptor);
    }

    @Override
    public void stop() {
        super.stop();
    }
}
