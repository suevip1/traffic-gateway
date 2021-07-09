package com.xl.traffic.gateway.common.node;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 用户信息
 * @Author: xl
 * @Date: 2021/6/21
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {


    /**用户id*/
    private String uid;

    /**设备id*/
    private String deviceId;











}
