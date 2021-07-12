package com.xl.traffic.gateway.core.config;

import lombok.Data;

@Data
public class GatewayCommonConfig {


    /**
     * ip访问次数限制/s
     */
    private int ip_limit_counts;


}

