package com.xl.traffic.gateway.core.server.ssl;

import com.xl.traffic.gateway.core.config.SslConfig;
import com.xl.traffic.gateway.core.config.SslEngineConfig;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

/**
 * ssl工厂
 * ssl 起动机
 *
 * @author: xl
 * @date: 2021/8/5
 **/
public interface SslEngineFactory {

    SSLEngine newSslEngine(SslEngineConfig sslEngineConfig);

    default void init(SslConfig sslConfig) throws SSLException {
    }
}
