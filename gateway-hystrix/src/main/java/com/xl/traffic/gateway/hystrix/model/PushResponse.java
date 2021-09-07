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
 * 服务端降级应答对象
 *
 * @author: xl
 * @date: 2021/6/28
 **/
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PushResponse {

    /**
     * 降级预案
     */
    private String sdsSchemeName;

    /**
     * 应用组
     */
    private String appGroupName;

    /**
     * 应用名称
     */
    private String appName;

    /**
     *
     */
    private Boolean changed;

    private Long version;

    private List<Strategy> strategies;

    private String errorMsg;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HeartBeatResponse{");
        sb.append("sdsSchemeName='").append(sdsSchemeName).append('\'');
        sb.append(", changed=").append(changed);
        sb.append(", version=").append(version);
        sb.append(", strategies=").append(strategies);
        sb.append(", errorMsg='").append(errorMsg).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
