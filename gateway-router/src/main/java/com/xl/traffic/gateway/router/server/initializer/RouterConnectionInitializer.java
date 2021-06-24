package com.xl.traffic.gateway.router.server.initializer;

import com.xl.traffic.gateway.core.helper.GatewayConfigHelper;
import com.xl.traffic.gateway.core.protocol.MessageDecoder;
import com.xl.traffic.gateway.core.protocol.MessageEncoder;
import com.xl.traffic.gateway.core.server.connection.AbstractConnectionInitializer;
import com.xl.traffic.gateway.core.server.connection.ConnectionFacade;
import com.xl.traffic.gateway.core.server.handler.NettyOnIdleHandler;
import com.xl.traffic.gateway.core.server.handler.NettyReciveHandler;
import com.xl.traffic.gateway.core.server.ssl.SslEngineFactory;
import com.xl.traffic.gateway.router.server.handler.RouterRpcHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class RouterConnectionInitializer extends AbstractConnectionInitializer {


    private final RouterRpcHandler serverHandler;

    private final NettyOnIdleHandler nettyOnIdleHandler;

    private final SslEngineFactory sslEngineFactory;

    private final NettyReciveHandler reciveHandler;


    public RouterConnectionInitializer(SslEngineFactory sslEngineFactory) {
        this.serverHandler = new RouterRpcHandler();
        this.nettyOnIdleHandler = new NettyOnIdleHandler();
        this.sslEngineFactory = sslEngineFactory;
        this.reciveHandler = new NettyReciveHandler();
    }


    @Override
    public void addDDOSHandlers(ConnectionFacade connectionFacade) {

    }

    @Override
    public void addTimeoutHandlers(ConnectionFacade connectionFacade) {
        connectionFacade.addHandler(new IdleStateHandler(60, 0, 0));
        connectionFacade.addHandler(nettyOnIdleHandler);
    }

    @Override
    public void addTcpHandlers(ConnectionFacade connectionFacade) {
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
