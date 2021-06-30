package com.xl.traffic.gateway.hystrix.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推送admin 数据统计信息
 *
 * @author: xl
 * @date: 2021/6/28
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PushCycleData {


    /**
     * 降级点名称
     */
    private String point;


    /**
     * 周期访问量（秒）
     */
    private long visitNum;


    /**
     * 周期异常数
     */
    private long exceptionNum;

    /**
     * 上一周期最高并发量
     */
    private Integer concurrentNum;

    /**
     * 周期超时量
     */
    private long timeoutNum;

    /**
     * 周期降级总次数
     */
    private long downgrateNum;


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PushCycleData{");
        sb.append("point='").append(point).append('\'');
        sb.append(", visitNum=").append(visitNum);
        sb.append(", exceptionNum=").append(exceptionNum);
        sb.append(", concurrentNum=").append(concurrentNum);
        sb.append(", timeoutNum=").append(timeoutNum);
        sb.append(", downgradeNum=").append(downgrateNum);
        sb.append('}');
        return sb.toString();
    }
}
