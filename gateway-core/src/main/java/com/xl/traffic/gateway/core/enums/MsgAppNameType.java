package com.xl.traffic.gateway.core.enums;

import com.xl.traffic.gateway.core.utils.GatewayConstants;

public enum MsgAppNameType {


    GATEWAY((byte) 1, GatewayConstants.GATEWAY),

    ;

    private byte type;
    private String name;

    MsgAppNameType(byte type, String name) {
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
