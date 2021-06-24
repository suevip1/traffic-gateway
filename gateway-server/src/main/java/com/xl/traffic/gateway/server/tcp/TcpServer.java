package com.xl.traffic.gateway.server.tcp;

import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.server.AbstractNettyServerInlization;
import com.xl.traffic.gateway.core.server.Server;
import com.xl.traffic.gateway.core.server.acceptor.ChannelAcceptor;
import com.xl.traffic.gateway.core.server.connection.ConnectionInitializer;
import com.xl.traffic.gateway.core.utils.GatewayPortConstants;
import com.xl.traffic.gateway.server.tcp.initializer.TcpConnectionInitializer;

/**
 * @author xuliang
 * @desc: tcp server
 * @date:2021-06-18
 */
public class TcpServer extends AbstractNettyServerInlization implements Server {

    public TcpServer() {
        super(AddressUtils.getV4IP(), GatewayPortConstants.TCP_PORT_OPEN);

    }

    @Override
    public void start() {
        //ssl 认证
        ssl();
        ConnectionInitializer connectionInitializer = new TcpConnectionInitializer(sslEngineFactory);
        ChannelAcceptor channelAcceptor = new ChannelAcceptor(connectionInitializer);
        //服务初始化
        init(channelAcceptor);
    }

    @Override
    public void stop() {
        super.stop();
    }
}
