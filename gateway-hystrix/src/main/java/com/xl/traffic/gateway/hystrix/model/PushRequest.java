package com.xl.traffic.gateway.hystrix.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 上传客户端上一分钟数据并从服务端拉取最新的策略
 *
 * @author: xl
 * @date: 2021/6/28
 **/
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PushRequest {

    /**
     * 客户端服务器IP
     */
    private String ip;

    /**
     * 客户端服务器hostname
     */
    private String hostname;

    /**
     * 应用组名称，同一个应用组下appName不能重复
     */
    private String appGroupName;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 版本号
     */
    private Long version;

    /**
     * 统计时间，即访问量降级点、异常量降级点和异常率降级点的统计时间
     * 比如2018-09-14 01:10:10上报的数据表示01:10:00 至 01:10:10 之间的数据
     */
    private Date statisticsCycleTime;

    /**
     * 降级点信息Map
     * 上传心跳数据使用
     */
    private Map<String, PushCycleData> pointInfoMap = new HashMap<>();

    /**
     * 客户端使用的降级点列表，用于从服务端拉取最新配置
     */
    private List<String> pointList;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HeartbeatRequest{");
        sb.append("ip='").append(ip).append('\'');
        sb.append(", hostname='").append(hostname).append('\'');
        sb.append(", appGroupName='").append(appGroupName).append('\'');
        sb.append(", appName='").append(appName).append('\'');
        sb.append(", version=").append(version);
        sb.append(", statisticsCycleTime=").append(statisticsCycleTime);
        sb.append(", pointInfoMap=").append(pointInfoMap);
        sb.append(", pointList=").append(pointList);
        sb.append('}');
        return sb.toString();
    }

}
