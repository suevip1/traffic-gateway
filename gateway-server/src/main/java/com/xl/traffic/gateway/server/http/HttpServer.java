package com.xl.traffic.gateway.server.http;


import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.server.AbstractNettyServerInlization;
import com.xl.traffic.gateway.core.server.Server;
import com.xl.traffic.gateway.core.server.acceptor.ChannelAcceptor;
import com.xl.traffic.gateway.core.server.connection.ConnectionInitializer;
import com.xl.traffic.gateway.core.utils.GatewayPortConstants;
import com.xl.traffic.gateway.server.http.initializer.HttpConnectionInitializer;

/**
 * @author xuliang
 * @desc: http server
 * @date:2021-06-18
 */
public class HttpServer extends AbstractNettyServerInlization implements Server {
    public HttpServer() {
        super(AddressUtils.getV4IP(), GatewayPortConstants.HTTP_PORT_OPEN);
    }

    @Override
    public void start() {
        ConnectionInitializer connectionInitializer = new HttpConnectionInitializer();
        ChannelAcceptor channelAcceptor = new ChannelAcceptor(connectionInitializer);
        init(channelAcceptor);
    }

    @Override
    public void stop() {
        super.stop();
    }
}
