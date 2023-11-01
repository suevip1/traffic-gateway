package com.xl.traffic.gateway.core.enums;

public enum ErrorCode {
    DOWNGRADE(1, "此请求已经被降级"),
    TOKEN_ERROR(2, "token过期断开连接"),


    ;


    private int code;
    private String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
