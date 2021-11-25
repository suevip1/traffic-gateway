package com.xl.traffic.gateway.core.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * LoginOutDTO rpc 通讯
 *
 * @author: xl
 * @date: 2021/7/6
 **/
@Data
public class LoginOutDTO implements Serializable {


    /**
     * 用户id
     */
    private String uid;

    /**
     * 用户登录设备 id+channelId
     */
    private String deviceId;
    /**
     * 用户源设备id
     */
    private String sourceDeviceId;


}
