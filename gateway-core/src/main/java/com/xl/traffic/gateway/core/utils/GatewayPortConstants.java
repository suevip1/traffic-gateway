package com.xl.traffic.gateway.core.utils;

public class GatewayPortConstants {

    /**
     * 上行消息 对外端口号 gateway tcp 端口号
     */
    public static int TCP_PORT_OPEN = 10001;
    /**
     * 上行消息 对外端口号 gateway http 端口号
     */
    public static int HTTP_PORT_OPEN = 10002;

    /**
     * 下行消息 内网端口号 gateway tcp 端口号
     */
    public static int TCP_PORT_INNER = 20001;

    /**
     * router 服务 tcp 端口号
     */
    public static int TCP_ROUTER_PORT = 20002;


    /**
     * admin 服务 tcp 端口号
     */
    public static int TCP_ADMIN_PORT = 20003;

    /**
     * monitor 服务 tcp 端口号
     */
    public static int TCP_MONITOR_PORT = 20004;



}
