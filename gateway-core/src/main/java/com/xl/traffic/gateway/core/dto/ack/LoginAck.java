package com.xl.traffic.gateway.core.dto.ack;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginAck implements Serializable {


    /**
     * 用户id
     */
    private String uid;

    /**
     * 用户登录设备 id+channelId
     */
    private String deviceId;
    /**
     * 用户源登录设备 id
     */
    private String sourceDeviceId;

    /**
     * 用户token
     */
    private String token;

    /**
     * 回话秘钥
     */
    private String sessionKey;

    /**
     * 服务时间
     */
    private long serverTime;

}
