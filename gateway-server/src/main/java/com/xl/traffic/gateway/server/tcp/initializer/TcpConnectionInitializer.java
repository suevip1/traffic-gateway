package com.xl.traffic.gateway.server.tcp.initializer;

import com.xl.traffic.gateway.core.helper.GatewayConfigHelper;
import com.xl.traffic.gateway.core.protocol.MessageDecoder;
import com.xl.traffic.gateway.core.protocol.MessageEncoder;
import com.xl.traffic.gateway.core.server.connection.AbstractConnectionInitializer;
import com.xl.traffic.gateway.core.server.connection.ConnectionFacade;
import com.xl.traffic.gateway.core.server.handler.NettyOnIdleHandler;
import com.xl.traffic.gateway.core.server.handler.NettyReciveHandler;
import com.xl.traffic.gateway.core.server.ssl.SslEngineFactory;
import com.xl.traffic.gateway.server.tcp.handler.NettyTcpServerHandler;
import io.netty.channel.ChannelHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class TcpConnectionInitializer extends AbstractConnectionInitializer {

    private final NettyTcpServerHandler serverHandler;

    private final NettyOnIdleHandler nettyOnIdleHandler;

    private final SslEngineFactory sslEngineFactory;
    private final NettyReciveHandler reciveHandler;


    public TcpConnectionInitializer(SslEngineFactory sslEngineFactory) {
        this.serverHandler = new NettyTcpServerHandler();
        this.nettyOnIdleHandler = new NettyOnIdleHandler();
        this.sslEngineFactory = sslEngineFactory;
        this.reciveHandler = new NettyReciveHandler();
    }

    @Override
    public void addDDOSHandlers(ConnectionFacade connectionFacade) {
        //添加 ssl 认证等
    }

    @Override
    public void addTimeoutHandlers(ConnectionFacade connectionFacade) {
        connectionFacade.addHandler(new IdleStateHandler(60, 0, 0));
        connectionFacade.addHandler(nettyOnIdleHandler);
    }

    @Override
    public void addTcpHandlers(ConnectionFacade connectionFacade) {
        //todo ssl认证/tcp消息监控
        if (GatewayConfigHelper.getInstance().getGateWayConfig().getSslConfig().isUseSsl()) {
            connectionFacade.addHandler(new SslHandler(sslEngineFactory.newSslEngine(GatewayConfigHelper.getInstance().getGateWayConfig()
                    .getSslConfig().getSslEngineConfig())));
        }
        // 添加tcp流控handler 例如 限流等
    }

    @Override
    public void addProtocolHandlers(ConnectionFacade connectionFacade) {
        //todo 添加具体的协议 消息编解码
        connectionFacade.addHandler(new MessageDecoder());
        connectionFacade.addHandler(new MessageEncoder());
    }

    @Override
    public void addBizHandlers(ConnectionFacade connectionFacade) {
        connectionFacade.addHandler(serverHandler);
        connectionFacade.addHandler(reciveHandler);
    }


}