package com.xl.traffic.gateway.admin.server;

import com.xl.traffic.gateway.admin.server.initializer.AdminRpcConnectionInitializer;
import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.server.AbstractNettyServerInlization;
import com.xl.traffic.gateway.core.server.Server;
import com.xl.traffic.gateway.core.server.acceptor.ChannelAcceptor;
import com.xl.traffic.gateway.core.server.connection.ConnectionInitializer;
import com.xl.traffic.gateway.core.utils.GatewayPortConstants;

public class AdminTcpServer extends AbstractNettyServerInlization implements Server {


    public AdminTcpServer() {
        super(AddressUtils.getInnetIp(), GatewayPortConstants.TCP_ADMIN_PORT);
    }

    @Override
    public void start() {
        ssl();
        ConnectionInitializer connectionInitializer = new AdminRpcConnectionInitializer(sslEngineFactory);
        ChannelAcceptor channelAcceptor = new ChannelAcceptor(connectionInitializer);
        init(channelAcceptor);
    }

    @Override
    public void stop() {
        super.stop();
    }
}
