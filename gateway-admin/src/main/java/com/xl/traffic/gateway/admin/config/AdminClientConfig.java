package com.xl.traffic.gateway.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gateway.config")
@Data
public class AdminClientConfig {
    private String appName;
    private String group;
    private String ip;
    private int tcpPort;
    private int weight;
    private int qps;
    private int cmdQps;
    private String signalType;
    private int rpcPoolSize;
    private String zip;
}
