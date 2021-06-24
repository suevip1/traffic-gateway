package com.xl.traffic.gateway.hystrix.model;

import lombok.Data;


/**
 * 检查数据，用来做策略判断
 *
 * @author: xl
 * @date: 2021/6/24
 **/
@Data
public class CheckData {

    /**
     * 降级点名称
     */
    private String point;

    /**
     * 当前时间
     */
    private long time;

    /**
     * 当前滑动周期内访问量
     */
    private long visitCount;

    /**
     * 当前秒访问量
     */
    private long curSecondVisitCount;

    /**
     * 上一秒访问量
     */
    private long lastSecondVisitCount;

    /**
     * 并发量获取结果
     */
    private Boolean concurrentAcquire = true;

    /**
     * 当前滑动周期内异常量
     */
    private long exceptionCount;

    /**
     * 当前滑动周期内超时量
     */
    private long timeoutCount;

    /**
     * 当前秒的令牌桶计数器
     */
    private long takeTokenBucketNum;

    /**
     * 当前滑动周期内降级数量
     */
    private long downgradeCount;


}
