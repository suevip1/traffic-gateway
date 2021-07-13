package com.xl.traffic.gateway.core.enums;

public enum MsgACKType {


    LOGIN((byte) 1, "登录回执"),

    ;

    private byte type;
    private String name;

    MsgACKType(byte type, String name) {
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
