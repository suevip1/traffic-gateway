package com.xl.traffic.gateway.common.node;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 服务节点信息
 * @Author: xl
 * @Date: 2021/6/21
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServerNodeInfo implements Serializable {
    /**
     * 唯一标识
     */
    private String id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 服务权重 负载均衡 灰度发布
     */
    private int weight;
    /**
     * 通信类型 tcp http
     */
    private String signalType;
    /**
     * 服务ip
     */
    private String ip;
    /**
     * 服务端口
     */
    private int port;
    /**
     * 压缩类型
     */
    private String zip;
    /**
     * 版本号
     */
    private String version;
    /**
     * 应用组
     */
    private String group;

    /**
     * 服务qps 全局
     */
    private int qps;
    /**
     * 服务 cmd qps
     */
    private int cmdQps;
    /**
     * 连接数
     */
    private int rpcPoolSize;

    /**
     * 连接索引编号
     */
    private int rpcServerIndex;

    /**
     * todo cmdQpsMap 配置cmd接口的qps大小<>暂时先不做啦</>
     */
    private Map<String, Integer> cmdQpsMap = new HashMap<>();

    /**当前服务的zkPath*/
    private String zkPath;

}
