package com.xl.traffic.gateway.core.dto;


import com.xl.traffic.gateway.core.model.SystemInfoModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 上报监控指标
 *
 * @author: xl
 * @date: 2021/7/7
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitorDTO implements Serializable {

    /**
     * 服务名称
     */
    private String serverName;

    /**
     * 服务qps
     */
    private int qps;

    /**
     * 服务连接数
     */
    private int connectNum;

    /**
     * 服务ip
     */
    private String gatewayIp;

    /**
     * 系统信息 cpu 内存等
     */
    private SystemInfoModel systemInfoModel;

    /**
     * 流量大小
     */
    private double bytes;


}
