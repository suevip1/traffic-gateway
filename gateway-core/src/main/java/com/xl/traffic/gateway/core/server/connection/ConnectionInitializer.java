package com.xl.traffic.gateway.core.server.connection;

public interface ConnectionInitializer {

    void initConnection(ConnectionFacade connectionFacade) throws Exception;

}
