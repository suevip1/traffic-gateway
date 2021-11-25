package com.xl.traffic.gateway.core.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * routerDto rpc 通讯
 *
 * @author: xl
 * @date: 2021/7/6
 **/
@Data
public class RouterDTO implements Serializable {


    public RouterDTO(String uid, String gatewayIp, String deviceId) {
        this.uid = uid;
        this.gatewayIp = gatewayIp;
        this.deviceId = deviceId;
    }

    /**
     * 用户id
     */
    private String uid;

    /**
     * 用户登录 gateway ip
     */
    private String gatewayIp;

    /**
     * 用户登录设备 id
     */
    private String deviceId;



}
