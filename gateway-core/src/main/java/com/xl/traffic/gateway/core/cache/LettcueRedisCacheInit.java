package com.xl.traffic.gateway.core.cache;

import com.xl.traffic.gateway.core.config.GatewayServerConfig;
import com.xl.traffic.gateway.core.redis.LettcueRedisApi;
import org.springframework.stereotype.Component;

@Component
public class LettcueRedisCacheInit {


    /**
     * 初始化lettcue redis
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/8/13
     **/
    public void initLettcueRedisCache() {
        LettcueRedisApi.initPool(GatewayServerConfig.getStringNotnull(GatewayServerConfig.REDIS_IP) + ":" +
                        GatewayServerConfig.getStringNotnull(GatewayServerConfig.REDIS_PORT),
                "", "", "");
    }
}
