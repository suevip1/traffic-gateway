package com.xl.traffic.gateway.core.server.ssl.impl;

import com.xl.traffic.gateway.core.config.SslEngineConfig;
import com.xl.traffic.gateway.core.server.ssl.SslEngineFactory;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

public abstract class SslEngineFactoryBase implements SslEngineFactory {

    protected void configureSslEngine(SSLEngine sslEngine, SslEngineConfig sslEngineConfig) {
        if (!sslEngineConfig.isDisableHttpsEndpointIdentificationAlgorithm()) {
            SSLParameters params = sslEngine.getSSLParameters();
            params.setEndpointIdentificationAlgorithm("HTTPS");
            sslEngine.setSSLParameters(params);
        }
    }
}
