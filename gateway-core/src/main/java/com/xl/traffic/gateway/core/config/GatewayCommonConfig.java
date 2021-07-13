package com.xl.traffic.gateway.core.config;

import lombok.Data;

@Data
public class GatewayCommonConfig {


    /**
     * ip访问次数限制/s
     */
    private int ip_limit_counts;

    /**
     * 是否启用安全加密解密
     */
    private boolean isSecurity;
    /**
     * token 安全秘钥
     */
    private String tokenSecurityKeyt;


}

