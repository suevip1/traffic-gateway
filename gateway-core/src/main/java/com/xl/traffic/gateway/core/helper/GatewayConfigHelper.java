package com.xl.traffic.gateway.core.helper;

import com.xl.traffic.gateway.core.config.GateWayConfig;
import lombok.Getter;
import lombok.Setter;

public class GatewayConfigHelper {


    private static class InstanceHolder {
        public static final GatewayConfigHelper instance = new GatewayConfigHelper();
    }

    public static GatewayConfigHelper getInstance() {
        return InstanceHolder.instance;
    }


    @Getter
    private GateWayConfig gateWayConfig;

    static {
        getInstance().loadConfig();
    }

    public void loadConfig() {

    }

}
