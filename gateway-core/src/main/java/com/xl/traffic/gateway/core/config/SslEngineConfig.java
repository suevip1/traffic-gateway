package com.xl.traffic.gateway.core.config;

public class SslEngineConfig {
    private boolean useOpenSsl;
    private boolean useInsecureTrustManager;
    private boolean disableHttpsEndpointIdentificationAlgorithm;

    private int sslSessionCacheSize;
    private int sslSessionTimeout;

    private String[] enabledProtocols;
    private String[] enabledCipherSuites;

    public boolean isUseOpenSsl() {
        return useOpenSsl;
    }

    public void setUseOpenSsl(boolean useOpenSsl) {
        this.useOpenSsl = useOpenSsl;
    }

    public boolean isUseInsecureTrustManager() {
        return useInsecureTrustManager;
    }

    public void setUseInsecureTrustManager(boolean useInsecureTrustManager) {
        this.useInsecureTrustManager = useInsecureTrustManager;
    }

    public boolean isDisableHttpsEndpointIdentificationAlgorithm() {
        return disableHttpsEndpointIdentificationAlgorithm;
    }

    public void setDisableHttpsEndpointIdentificationAlgorithm(boolean disableHttpsEndpointIdentificationAlgorithm) {
        this.disableHttpsEndpointIdentificationAlgorithm = disableHttpsEndpointIdentificationAlgorithm;
    }

    public int getSslSessionCacheSize() {
        return sslSessionCacheSize;
    }

    public void setSslSessionCacheSize(int sslSessionCacheSize) {
        this.sslSessionCacheSize = sslSessionCacheSize;
    }

    public int getSslSessionTimeout() {
        return sslSessionTimeout;
    }

    public void setSslSessionTimeout(int sslSessionTimeout) {
        this.sslSessionTimeout = sslSessionTimeout;
    }

    public String[] getEnabledProtocols() {
        return enabledProtocols;
    }

    public void setEnabledProtocols(String[] enabledProtocols) {
        this.enabledProtocols = enabledProtocols;
    }

    public String[] getEnabledCipherSuites() {
        return enabledCipherSuites;
    }

    public void setEnabledCipherSuites(String[] enabledCipherSuites) {
        this.enabledCipherSuites = enabledCipherSuites;
    }

    public boolean validate() {
        return true;
    }
}
