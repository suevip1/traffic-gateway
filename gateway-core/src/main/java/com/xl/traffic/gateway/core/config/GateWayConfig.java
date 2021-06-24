package com.xl.traffic.gateway.core.config;


import lombok.Data;

@Data
public class GateWayConfig {


    private HttpConfig httpConfig;


    private TcpConfig tcpConfig;

    private SslConfig sslConfig;


}
