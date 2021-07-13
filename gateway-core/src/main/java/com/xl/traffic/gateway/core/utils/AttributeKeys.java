package com.xl.traffic.gateway.core.utils;

import io.netty.util.AttributeKey;

/**
 * rpc 连接缓存信息
 *
 * @author: xl
 * @date: 2021/6/24
 **/
public interface AttributeKeys {


    /**
     * 连接池连接标记key
     */
    AttributeKey<String> RPC_POOL_KEY = AttributeKey.valueOf("RpcPoolKey");

    /**
     * RPC服务器IP
     */
    AttributeKey<String> RPC_SERVER = AttributeKey.valueOf("RpcServer");

    /**
     * Rpc Port
     */
    AttributeKey<Integer> RPC_PORT = AttributeKey.valueOf("RpcPort");

    /**
     * Rpc ChannelId
     */
    AttributeKey<String> RPC_CHANNELID = AttributeKey.valueOf("RpcChannelId");

    /**
     * RPC连接编号
     */
    AttributeKey<Integer> RPC_INDEX = AttributeKey.valueOf("RpcIndex");


    /**
     * RPC GROUP应用组
     */
    AttributeKey<String> RPC_GROUP = AttributeKey.valueOf("RpcGroup");


    /**
     * 用户登录设备DeviceId + ChannelId
     */
    AttributeKey<String> DEVICE_ID = AttributeKey.valueOf("deviceChannelId");


    /**
     * 用户登录原设备DeviceId
     */
    AttributeKey<String> SOURCE_DEVICE_ID = AttributeKey.valueOf("sourceDeviceId");

    /**
     * 用户名称
     */
    AttributeKey<String> USER_ID = AttributeKey.valueOf("userId");


    /**
     * SessionKey
     */
    AttributeKey<String> SESSION_KEY = AttributeKey.valueOf("sessionKey");
    /**
     * token
     */
    AttributeKey<String> TOKEN = AttributeKey.valueOf("token");



}
