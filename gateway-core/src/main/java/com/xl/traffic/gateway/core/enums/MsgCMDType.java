package com.xl.traffic.gateway.core.enums;

public enum MsgCMDType {


    HEAT_CMD((byte) 1, "心跳检测"),
    UPLOAD_DOWNGRATE_DATA_CMD((byte) 2, "上传降级统计信息"),
    PULL_GATEWAY_HEALTH_DATA_CMD((byte) 3, "拉取gateway健康指标信息"),
    LOGIN_CMD((byte) 4, "登录"),
    LOGIN_OUT_CMD((byte) 5, "退出登录"),
    DISCONNECT((byte) 6, "断开连接"),
    BLACK_IP_CMD((byte) 7, "黑名单"),
    REGISTER_MONITOR_TASK((byte) 8, "注册monitor任务"),
    HYSTRIX_NOTIFY((byte) 9, "降级通知"),

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
