package com.xl.traffic.gateway.core.config;

public class SslConfig {
    private boolean useSsl;

    private String keyStorePassword;
    private String keyManagerPassword;
    private String certFile;

    private SslEngineConfig sslEngineConfig;

    public void setUseSsl(boolean useSsl) {
        this.useSsl = useSsl;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyManagerPassword(String keyManagerPassword) {
        this.keyManagerPassword = keyManagerPassword;
    }

    public String getKeyManagerPassword() {
        return keyManagerPassword;
    }

    public void setCertFile(String certFile) {
        this.certFile = certFile;
    }

    public String getCertFile() {
        return certFile;
    }

    public void setSslEngineConfig(SslEngineConfig sslEngineConfig) {
        this.sslEngineConfig = sslEngineConfig;
    }

    public SslEngineConfig getSslEngineConfig() {
        return sslEngineConfig;
    }

    public boolean validate() {
        if (!useSsl) {
            return true;
        }

        return keyManagerPassword != null && keyManagerPassword.length() != 0 &&
                keyStorePassword != null && keyStorePassword.length() != 0 &&
                certFile != null && certFile.length() != 0 && sslEngineConfig.validate();
    }
}
