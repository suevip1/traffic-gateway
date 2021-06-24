package com.xl.traffic.gateway.core.server.ssl.impl;


import com.xl.traffic.gateway.core.config.SslEngineConfig;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class JsseSslEngineFactory extends SslEngineFactoryBase {
    private final SSLContext sslContext;

    public JsseSslEngineFactory(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    public SSLEngine newSslEngine(SslEngineConfig sslEngineConfig) {
        SSLEngine sslEngine = sslContext.createSSLEngine();
        configureSslEngine(sslEngine, sslEngineConfig);
        return sslEngine;
    }
}
