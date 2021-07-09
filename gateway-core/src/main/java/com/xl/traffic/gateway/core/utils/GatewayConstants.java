package com.xl.traffic.gateway.core.utils;

/**
 * @Description:常量类
 * @Author: xl
 * @Date: 2021/6/21
 **/
public class GatewayConstants {


    /**
     * gateway
     */
    public static String GATEWAY = "gateway";
    public static String GATEWAY_GROUP = "gateway_group";
    public static String ROUTER = "router";
    public static String ROUTER_GROUP = "router_group";
    public static String ADMIN = "admin";
    public static String ADMIN_GROUP = "admin_group";
    public static String MONITOR = "monitor";
    public static String MONITOR_GROUP = "monitor_group";

    /**服务注册路径层级定义，共3层，第一层为 server 第二层为具体业务名称*/
    /**
     * 默认路径
     */
    public static String ROOT_RPC_SERVER_PATH_PREFIX = "/server/";
    /**
     * gateway 服务 zk 路径
     */
    public static String GATEWAY_ZK_ROOT_PATH = ROOT_RPC_SERVER_PATH_PREFIX + GATEWAY;
    /**
     * router 服务 zk 路径
     */
    public static String ROUTER_ZK_ROOT_PATH = ROOT_RPC_SERVER_PATH_PREFIX + ROUTER;

    /**
     * admin 服务 zk 路径
     */
    public static String ADMIN_ZK_ROOT_PATH = ROOT_RPC_SERVER_PATH_PREFIX + ADMIN;

    /**
     * monitor 服务 zk 路径
     */
    public static String MONITOR_ZK_ROOT_PATH = ROOT_RPC_SERVER_PATH_PREFIX + MONITOR;


    /**
     * gateway配置
     */
    public static String CONFIG = "/config";

    /**
     * 消息协议最大长度
     */
    public static int MSG_MAX_LENGTH = 1024 * 1024 * 32;

    /**
     * 消息协议默认长度
     */
    public static int MSG_LENGTH = 7;


    /**
     * 缓存分隔符
     */
    public static String SEQ = "__";

    /**
     * 重试队列大小
     */
    public static int RETRY_QUEUE_COUNT = 3;

    /**
     * 默认连接数
     */
    public static int RPC_POOL_SIZE = 3;

    /**
     * 默认权重值
     */
    public static int WEIGHT = 50;

    /**
     * 缓存最大数量 单台最大连接数 100万
     */
    public static int CONNECT_CACHE_MAX_SIZE = 100 * 10000;


    /**
     * 1秒内的毫秒数
     */
    public static final long MILLISECOND_IN_SECOND = 1000;

    /**
     * tcp协议
     */
    public static final String TCP = "tcp";
    /**
     * http协议
     */
    public static final String HTTP = "http";

    /**
     * 连接用户前缀
     */
    public static String CONN_PREFIX = "conn:";                   //在线用户(hash)


    /**
     * 定时多长时间上报一次gateway 服务健康数据信息，单位s
     */
    public static int REPORT_GATEWAY_HEALTH_DATA_TIME = 60;

}
