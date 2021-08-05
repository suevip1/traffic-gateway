package com.xl.traffic.gateway.core.server.ssl.impl;

import com.xl.traffic.gateway.core.config.SslConfig;
import com.xl.traffic.gateway.core.config.SslEngineConfig;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.io.*;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;

/**
 * 默认ssl实现类
 *
 * @author: xl
 * @date: 2021/8/5
 **/
public class DefaultSslEngineFactory extends SslEngineFactoryBase {
    private volatile SslContext sslContext;

    @Override
    public SSLEngine newSslEngine(SslEngineConfig sslEngineConfig) {
        SSLEngine sslEngine = sslContext.newEngine(ByteBufAllocator.DEFAULT);
        configureSslEngine(sslEngine, sslEngineConfig);
        return sslEngine;
    }

    @Override
    public void init(SslConfig sslConfig) throws SSLException {
        KeyManagerFactory kmf = loadCert(sslConfig);
        if (kmf == null) {
            throw new SSLException("load cert file failed");
        }
        sslContext = buildSslContext(sslConfig, kmf);
    }

    private SslContext buildSslContext(SslConfig sslConfig, KeyManagerFactory kmf) throws SSLException {
        SslEngineConfig sslEngineConfig = sslConfig.getSslEngineConfig();

        SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(kmf)
                .sslProvider(sslEngineConfig.isUseOpenSsl() ? SslProvider.OPENSSL : SslProvider.JDK);

        if (sslEngineConfig.getSslSessionCacheSize() > 0) {
            sslContextBuilder.sessionCacheSize(sslEngineConfig.getSslSessionCacheSize());
        }

        if (sslEngineConfig.getSslSessionTimeout() > 0) {
            sslContextBuilder.sessionTimeout(sslEngineConfig.getSslSessionTimeout());
        }

        String[] protocols = sslEngineConfig.getEnabledProtocols();
        if (protocols != null && protocols.length > 0) {
            sslContextBuilder.protocols(sslEngineConfig.getEnabledProtocols());
        }

        String[] cipherSuites = sslEngineConfig.getEnabledCipherSuites();
        if (cipherSuites != null && cipherSuites.length > 0) {
            sslContextBuilder.ciphers(Arrays.asList(sslEngineConfig.getEnabledCipherSuites()));
        }

        if (sslEngineConfig.isUseInsecureTrustManager()) {
            sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
        }

        return sslContextBuilder.build();
    }

    private KeyManagerFactory loadCert(SslConfig sslConfig) {
        final String pk12File = sslConfig.getCertFile();
        final String keyStorePassword = sslConfig.getKeyStorePassword();
        final String keyManagerPassword = sslConfig.getKeyManagerPassword();
        KeyManagerFactory kmf = null;

        try {
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        } catch (NoSuchAlgorithmException ignore) {
            // should not happen
        }

        if (kmf == null) {
            return null;
        }

        InputStream pk12InputStream = null;
        try {
            //加载客户端证书
            pk12InputStream = openPK12AsStream(pk12File);
            if (pk12InputStream == null) {
                return null;
            }

            //密钥库KeyStore
            final KeyStore ks = KeyStore.getInstance("PKCS12");
            //加载服务端的KeyStore  ；sNetty是生成仓库时设置的密码，用于检查密钥库完整性的密码
            ks.load(pk12InputStream, keyStorePassword.toCharArray());

            //初始化密钥管理器
            kmf.init(ks, keyManagerPassword.toCharArray());

        } catch (NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | KeyStoreException | IOException ex) {
        } finally {
            try {
                if (pk12InputStream != null) {
                    pk12InputStream.close();
                }
            } catch (IOException ignore) {
            }
        }

        return kmf;
    }

    private InputStream openPK12AsStream(String pk12Path) throws FileNotFoundException {
        URL url = getClass().getClassLoader().getResource(pk12Path);
        if (url != null) {
            return getClass().getClassLoader().getResourceAsStream(pk12Path);
        }

        File file = new File(pk12Path);
        if (file.exists()) {
            return new FileInputStream(file);
        }

        return null;
    }
}
