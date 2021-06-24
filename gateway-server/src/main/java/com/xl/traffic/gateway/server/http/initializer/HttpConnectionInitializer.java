package com.xl.traffic.gateway.server.http.initializer;

import com.xl.traffic.gateway.core.server.connection.AbstractConnectionInitializer;
import com.xl.traffic.gateway.core.server.connection.ConnectionFacade;
import com.xl.traffic.gateway.core.server.handler.NettyOnIdleHandler;
import com.xl.traffic.gateway.server.http.handler.NettyHttpServerHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class HttpConnectionInitializer extends AbstractConnectionInitializer {

    private final NettyHttpServerHandler serverHandler;
    private final NettyOnIdleHandler nettyOnIdleHandler;


    public HttpConnectionInitializer() {
        this.serverHandler = new NettyHttpServerHandler();
        this.nettyOnIdleHandler = new NettyOnIdleHandler();
    }

    @Override
    public void addDDOSHandlers(ConnectionFacade connectionFacade) {

    }

    @Override
    public void addTimeoutHandlers(ConnectionFacade connectionFacade) {
        connectionFacade.addHandler(new IdleStateHandler(0, 0, 60, TimeUnit.MILLISECONDS));
        connectionFacade.addHandler(nettyOnIdleHandler);
    }

    @Override
    public void addTcpHandlers(ConnectionFacade connectionFacade) {

    }

    @Override
    public void addProtocolHandlers(ConnectionFacade connectionFacade) {
        connectionFacade.addHandler(new HttpServerCodec());
        connectionFacade.addHandler(new HttpObjectAggregator(65536));
        connectionFacade.addHandler(new ChunkedWriteHandler());
    }

    @Override
    public void addBizHandlers(ConnectionFacade connectionFacade) {
        connectionFacade.addHandler(serverHandler);
    }
}
