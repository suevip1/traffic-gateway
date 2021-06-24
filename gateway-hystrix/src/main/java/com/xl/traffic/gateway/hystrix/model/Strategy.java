package com.xl.traffic.gateway.hystrix.model;

import lombok.Data;

/**
 * 降级策略对象
 *
 * @author: xl
 * @date: 2021/6/24
 **/
@Data
public class Strategy {

    /**
     * 降级点名称
     */
    private String point;

    /**
     * 访问量阈值，-1表示未生效，0表示彻底关闭流量（相当于开关）
     * 注意：只能全能降级点才会使用
     */
    private long visitThreshold = -1l;

    /**
     * 并发量阈值，-1表示未生效
     * 注意：只能全能降级才会使用
     */

    private int concurrentThreshold = -1;
    /**
     * 异常量阈值，-1表示未生效
     * 注意：只能全能降级才会使用
     */

    private long exceptionThreshold = -1;
    /**
     * 超时时间阈值，-1表示未生效
     * 和{@link #timeoutCountThreshold} 配合使用
     */
    private long timeoutThreshold = -1;

    /**
     * 超时次数阈值，如果超过 {@link #timeoutThreshold}的访问量达到该值，就应该降级，-1表示未生效
     */
    private long timeoutCountThreshold = -1;

    /**
     * 异常率阈值，取值为[0-100],15表示异常率阈值为15%，该属性为异常率降级点独有，-1表示不对异常率进行降级
     * 和{@link #exceptionRateStart} 配合使用
     */
    private int exceptionRateThreshold = -1;

    /**
     * 异常率降级判断的起点（标准），和{@link #exceptionRateThreshold} 一起使用，访问量超过该值才开始计算异常率，避免采样失去准确性
     */
    private long exceptionRateStart = 0;

    /**
     * 每个令牌桶1秒内能生成多少个令牌，-1表示不生效
     */
    private int tokenBucketGeneratedTokenInSecond = -1;
    /**
     * 每个令牌桶中最多能存储多少个令牌，-1表示不生效
     */
    private int tokenBucketSize = -1;

    /**
     * 访问量降级，异常量降级，异常率降级中的降级延迟时间，表示降级的持续时间，单位毫秒，最小值为10000
     */
    private long delayTime = -1;

    /**
     * 异常量降级特有的降级延迟期间重试时间周期，如果小于等于0表示不重试，单位毫秒，最小值10000
     */
    private long retryInterval = -1;

    /**
     * 降级比率，取值[0-100]
     * 例如：值为15表示没100笔请求将有15笔拒绝掉
     * 默认值：100
     */
    private int downgradeRate = 100;

    /**
     * 返回值字符串，一般为json
     */
    private String returnValueStr = null;

    /**
     * 压测流量降级，0-压测流量对策略降级不产生影响，1-压测流量直接强制降级
     */
    private int pressureTestDowngrade = 0;


}
