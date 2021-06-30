package com.xl.traffic.gateway.hystrix.service;

import com.xl.traffic.gateway.hystrix.counter.PowerfulCycleTimeCounter;
import com.xl.traffic.gateway.hystrix.model.Strategy;
import com.xl.traffic.gateway.hystrix.model.VisitValue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 计数器服务类
 * {@link com.xl.traffic.gateway.hystrix.counter.PowerfulCycleTimeCounter}
 *
 * @author: xl
 * @date: 2021/6/25
 **/
@Slf4j
public class PowerfulCounterService {


    private final static PowerfulCounterService counterService = new PowerfulCounterService();

    /**
     * 单例
     */
    public static PowerfulCounterService getInstance() {
        return counterService;
    }


    /**
     * 计数器Map，key-降级点名称，value访问计数器对象
     */
    @Getter
    private final ConcurrentHashMap<String, PowerfulCycleTimeCounter> pointCounterMap = new ConcurrentHashMap<>();


    /**
     * 设置当前降级点的秒访问量计数器+1
     *
     * @param point 降级点名称
     * @param time  当前时间
     * @return: 返回当前降级点的当前时间内的调用访问量
     * @author: xl
     * @date: 2021/6/25
     **/
    public VisitValue visitAddAndGet(String point, long time) {
        PowerfulCycleTimeCounter powerfulCycleTimeCounter = getOrCreatePoint(point);
        return powerfulCycleTimeCounter.visitAddAndGet(time);

    }

    /**
     * 获取当前降级点的上一周期内的秒访问量调用总值
     *
     * @param point 降级点名称
     * @param time  当前时间
     * @return: long 返回当前降级点的上一周期的所有桶的调用秒访问总量
     * @author: xl
     * @date: 2021/6/25
     **/
    public long getLastSecondVisitBucketValue(String point, long time) {
        PowerfulCycleTimeCounter powerfulCycleTimeCounter = getOrCreatePoint(point);
        return powerfulCycleTimeCounter.getLastSecondVisitBucketValue(time);
    }

    /**
     * 设置当前降级点的当前毫秒计数器并发次数+1
     *
     * @param point 降级点名称
     * @param time  当前时间
     * @return: boolean true 成功 false 超出并发法阈值
     * @author: xl
     * @date: 2021/6/25
     **/
    public boolean concurrentAcquire(String point, long time) {
        PowerfulCycleTimeCounter powerfulCycleTimeCounter = getOrCreatePoint(point);
        return powerfulCycleTimeCounter.concurrentAcquire(time);

    }

    /**
     * 设置当前降级点并发计数器-1
     *
     * @param point 降级点名称
     * @return: void
     * @author: xl
     * @date: 2021/6/25
     **/
    public void concurrentRelease(String point) {
        PowerfulCycleTimeCounter powerfulCycleTimeCounter = getOrCreatePoint(point);
        powerfulCycleTimeCounter.concurrentRelease();
    }

    /**
     * 设置当前降级点当前毫秒异常计数器+1
     *
     * @param point 降级点名称
     * @param time  当前时间
     * @return: long 返回当前降级点周期内的所有桶的异常调用总数
     * @author: xl
     * @date: 2021/6/25
     **/
    public long exceptionAddAndGet(String point, long time) {
        PowerfulCycleTimeCounter powerfulCycleTimeCounter = getOrCreatePoint(point);
        return powerfulCycleTimeCounter.exceptionAddAndGet(time);
    }

    /**
     * 获取当前降级点的当前时间所在桶的异常调用次数
     *
     * @param point 降级点名称
     * @param time  当前时间
     * @return: long  返回当前降级点的当前时间所在桶的异常调用次数
     * @author: xl
     * @date: 2021/6/25
     **/
    public long getExceptionValue(String point, long time) {
        PowerfulCycleTimeCounter powerfulCycleTimeCounter = getOrCreatePoint(point);
        return powerfulCycleTimeCounter.getExceptionValue(time);
    }


    /**
     * 设置当前降级点的当前时间超时计数器+1
     *
     * @param point 降级点名称
     * @param time  当前时间
     * @return: 返回当前降级点当前周期内的所有桶的超时调用总次数
     * @author: xl
     * @date: 2021/6/25
     **/
    public long timeoutAddAndGet(String point, long time) {
        PowerfulCycleTimeCounter powerfulCycleTimeCounter = getOrCreatePoint(point);
        return powerfulCycleTimeCounter.timeoutAddAndGet(time);
    }

    /**
     * 获取当前降级点名称的当前时间的超时次数
     *
     * @param point 降级点名称
     * @param time  当前时间
     * @return: long  返回当前降级点名称的当前时间的超时次数
     * @author: xl
     * @date: 2021/6/25
     **/
    public long getTimeoutValue(String point, long time) {
        PowerfulCycleTimeCounter powerfulCycleTimeCounter = getOrCreatePoint(point);
        return powerfulCycleTimeCounter.getTimeoutValue(time);
    }

    /**
     * 尝试消耗一个token令牌
     *
     * @param point 降级点名称
     * @param time  当前时间
     * @return: 返回当前降级点的当前时间消耗的令牌次数
     * @author: xl
     * @date: 2021/6/25
     **/
    public long tokenBucketAddAndGet(String point, long time) {
        PowerfulCycleTimeCounter powerfulCycleTimeCounter = getOrCreatePoint(point);
        return powerfulCycleTimeCounter.tokenBucketInvokeAddAndGet(time);
    }

    /**
     * 设置当前降级点当前时间内的降级次数+1
     *
     * @param point 降级点名称
     * @param time  当前时间
     * @return: 返回当前降级点当前周期内的调用降级总次数
     * @author: xl
     * @date: 2021/6/25
     **/
    public long downgrateAddAndGet(String point, long time) {
        PowerfulCycleTimeCounter powerfulCycleTimeCounter = getOrCreatePoint(point);
        return powerfulCycleTimeCounter.downgrateAddAndGet(time);
    }

    /**
     * 获取当前降级点的当前周期内的降级总次数
     *
     * @param point 降级点名称
     * @param time  当前时间
     * @return: 返回当前降级点的当前周期内的所有桶的降级总次数
     * @author: xl
     * @date: 2021/6/25
     **/
    public long getCurCycleDowngrateValue(String point, long time) {
        PowerfulCycleTimeCounter powerfulCycleTimeCounter = getOrCreatePoint(point);
        return powerfulCycleTimeCounter.getDowngrateValue(time);
    }


    /**
     * 获取或创建一个新的降级点名称对应的访问量计数器
     *
     * @param point
     * @return: 返回该降级点的访问量计数器
     * @author: xl
     * @date: 2021/6/25
     **/
    public PowerfulCycleTimeCounter getOrCreatePoint(String point) {
        PowerfulCycleTimeCounter cycleTimeCounter = pointCounterMap.get(point);
        if (null == cycleTimeCounter) {
            //创建一个新的降级点
            cycleTimeCounter = addPoint(point);
        }
        return cycleTimeCounter;

    }

    /**
     * 主动更新所有降级点的降级策略的并发阈值
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/6/25
     **/
    public void updateConcurrent() {
        for (Map.Entry<String, Strategy> entry : StrategyService.getInstance().getStrategyMap().entrySet()) {
            if (entry.getValue().getConcurrentThreshold() < 0) {
                continue;
            }
            PowerfulCycleTimeCounter powerfulCycleTimeCounter = getOrCreatePoint(entry.getKey());
            if (entry.getValue().getConcurrentThreshold() >= 0) {
                powerfulCycleTimeCounter.updateConcurrentThreshold(entry.getValue().getConcurrentThreshold());
            } else {
                /**如果是小于0的值，例如-1，那就是表明不开启并发限流功能*/
            }
        }
    }


    /**
     * 主动更新单个降级点的降级策略的并发阈值
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/6/25
     **/
    public void updateConcurrentByPoint(String point, int concurrentThreshold) {
        PowerfulCycleTimeCounter powerfulCycleTimeCounter = getOrCreatePoint(point);
        if (concurrentThreshold >= 0) {
            powerfulCycleTimeCounter.updateConcurrentThreshold(concurrentThreshold);
        } else {
            /**如果是小于0的值，例如-1，那就是表明不开启并发限流功能*/
            powerfulCycleTimeCounter.updateConcurrentThreshold(Integer.MAX_VALUE);
        }

    }


    /**
     * 新增降级点
     *
     * @param point 降级点名称
     * @return: 返回该降级点的访问计数器
     * @author: xl
     * @date: 2021/6/25
     **/
    public PowerfulCycleTimeCounter addPoint(String point) {
        if (pointCounterMap.putIfAbsent(point, new PowerfulCycleTimeCounter()) == null) {
            log.info("PowerfulCounterService 新增降级点:" + point);
        }
        return pointCounterMap.get(point);
    }


}
