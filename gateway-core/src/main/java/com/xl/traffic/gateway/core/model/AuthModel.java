package com.xl.traffic.gateway.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthModel {


    /**
     * 用户id
     */
    private String uid;

    /**
     * 平台端用户token
     */
    private int port;


}
