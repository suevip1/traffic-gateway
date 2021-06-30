package com.xl.traffic.gateway.core.enums;

public enum MsgCMDType {


    HEAT_CMD((byte) 1, "心跳检测"),
    UPLOAD_DOWNGRATE_DATA_CMD((byte) 2, "上传降级统计信息"),

    ;

    private byte type;
    private String name;

    MsgCMDType(byte type, String name) {
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
