package com.xl.traffic.gateway.core.enums;

import com.xl.traffic.gateway.core.utils.GatewayConstants;

public enum MsgGroupType {


    GATEWAY((byte) 1, GatewayConstants.GATEWAY_GROUP),
    MONITOR((byte) 2, GatewayConstants.MONITOR_GROUP),

    ;

    private byte type;
    private String name;

    MsgGroupType(byte type, String name) {
        this.type = type;
        this.name = name;
    }


    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
