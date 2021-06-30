package com.xl.traffic.gateway.hystrix.downgrade;

import com.xl.traffic.gateway.core.utils.AssertUtil;
import com.xl.traffic.gateway.core.utils.TimeStatisticsUtil;
import com.xl.traffic.gateway.hystrix.AbstractDowngradeClient;
import com.xl.traffic.gateway.hystrix.enums.DowngradeActionType;
import com.xl.traffic.gateway.hystrix.model.CheckData;
import com.xl.traffic.gateway.hystrix.model.Strategy;
import com.xl.traffic.gateway.hystrix.model.VisitValue;
import com.xl.traffic.gateway.hystrix.notify.DowngrateActionNotify;
import com.xl.traffic.gateway.hystrix.service.*;
import com.xl.traffic.gateway.hystrix.strategy.AbstractStrategyExecutor;
import com.xl.traffic.gateway.hystrix.strategy.DefaultStrategyExecutorBuilder;
import com.xl.traffic.gateway.hystrix.strategy.StrategyExecutorBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.ThreadLocalRandom;

/**
 * downgrade服务降级客户端执行
 *
 * @author: xl
 * @date: 2021/6/24
 **/
@Slf4j
public class CommondDowngradeClient extends AbstractDowngradeClient {

    PowerfulCounterService powerfulCounterService = PowerfulCounterService.getInstance();


    /**
     * 记录降级的开始时间
     */
    private ThreadLocal<Long> downgrateStartTime = new ThreadLocal<>();

    /**
     * 调用链
     */
    private volatile AbstractStrategyExecutor strategyExecutor;


    public CommondDowngradeClient(String appGroupName, String appName) {
        super(appGroupName, appName);
        /**初始化策略链*/
        initStrategyChain();

        /**初始化配置策略信息*/
        PullAndPushService.createOnlyOne(appGroupName, appName);
    }


    /**
     * 初始化策略
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/6/28
     **/
    private void initStrategyChain() {
        String strategyExecutorName = "";

        try {
            ServiceLoader<StrategyExecutorBuilder> strategyExecutorBuilders = ServiceLoader.load(StrategyExecutorBuilder.class);
            Iterator<StrategyExecutorBuilder> strategyExecutorBuilderIterable = strategyExecutorBuilders.iterator();
            /**注意这里是if ，只能取到一个*/
            if (strategyExecutorBuilderIterable.hasNext()) {
                StrategyExecutorBuilder strategyExecutorBuilder = strategyExecutorBuilderIterable.next();
                strategyExecutor = strategyExecutorBuilder.build();
                strategyExecutorName = strategyExecutorBuilder.getClass().getName();
            }
            if (strategyExecutor == null) {
                /**构建默认的策略链*/
                strategyExecutor = new DefaultStrategyExecutorBuilder().build();
            }
            log.info("CommondDowngradeClient use strategy executor chain:{}", strategyExecutorName);
        } catch (Exception exception) {
            log.info("CommondDowngradeClient initStrategyChain error:{}", exception);

        }


    }


    /**
     * 服务降级入口
     *
     * @param point 降级点名称，每个应用内必须唯一
     * @return: boolean false-不需要降级，正常访问；true-需要降级
     * @author: xl
     * @date: 2021/6/24
     **/
    @Override
    public boolean shouldDowngrade(String point) {
        AssertUtil.notBlack(point, "降级点不能为空！");
        try {
            long now = System.currentTimeMillis();
            /**
             * 记录开始时间
             */
            TimeStatisticsUtil.startTime();

            /**
             * 记录降级的开始时间
             */
            setDowngradeStartTime(now);

            /**秒访问量+1*/
            VisitValue visitValue = powerfulCounterService.visitAddAndGet(point, now);
            /**获取上一秒的访问量*/
            long lastSecondVisitCount = powerfulCounterService.getLastSecondVisitBucketValue(point, now);
            /**并发访问量+1 true 成功，false 失败*/
            boolean concurrentAcquire = powerfulCounterService.concurrentAcquire(point, now);
            /**当前滑动周期的异常次数*/
            long exceptionCount = powerfulCounterService.getExceptionValue(point, now);
            /**当前周期的异常调用次数*/
            long timeoutCount = powerfulCounterService.getTimeoutValue(point, now);
            /**尝试消耗一个当前秒的令牌，并获取当前桶已消耗的令牌数*/
            long takeTokenBucketNum = powerfulCounterService.tokenBucketAddAndGet(point, now);
            /**获取当前周期的降级次数*/
            long downgrateCount = powerfulCounterService.getCurCycleDowngrateValue(point, now);
            /**降级判断，是否需要降级处理*/
            return checkDowngrate(point, visitValue.getSlidingCycleValue(), visitValue.getBucketValue(), lastSecondVisitCount
                    , concurrentAcquire, exceptionCount, timeoutCount, takeTokenBucketNum
                    , downgrateCount, now);
        } catch (Exception ex) {
            log.error("CommondDowngradeClient shouldDowngrade point:{} error:{}", point, ex);
        }
        return false;
    }


    /**
     * 根据SdsDowngradePointFactory配置的异常列表，来判断是否增加异常量访问计数器
     * <p>
     * 注意：用户可以通过 {@see SdsPointStrategyConfig#setDowngradeExceptions(String, java.util.List)}
     * 和  {@see SdsPointStrategyConfig#setDowngradeExceptExceptions(String, java.util.List)}
     * 来设置失败异常列表和排除异常列表。
     *
     * @param point     降级点
     * @param throwable 如果exception为null，则直接进行异常量计数器+1
     * @return 当前异常数
     */
    @Override
    public void exceptionSign(String point, Throwable throwable) {

        AssertUtil.notBlack(point, "降级点不能为空！");
        try {

            long startTime = getDowngradeStartTime();
            if (throwable != null && DowngrateExceptionService.getInstance().isDowngradeException(point, throwable)) {

                /**出现异常，继续降级延迟*/
                DowngrateDelayService.getInstance().continueDowngrateDelay(point, startTime);

                /**异常计数器+1*/
                powerfulCounterService.exceptionAddAndGet(point, startTime);
            }
        } catch (Exception ex) {
            log.error("CommondDowngradeClient exceptionSign point:{} error:{}", point, ex);
        }
    }

    /**
     * 降级出口
     * 该方法在finally语句块中填写，必须调用，否则会导致某些资源没有被释放！
     *
     * @param point 降级点名称
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    @Override
    public void downgradeFinally(String point) {
        try {
            /**step1: 获取业务的耗时时间*/
            long consumerTime = TimeStatisticsUtil.getrConsumerTime();
            /**step2: 并发数-1*/
            powerfulCounterService.concurrentRelease(point);
            /**step3：获取策略配置*/
            Strategy strategy = StrategyService.getInstance().getStrategy(point);
            if (strategy == null || strategy.getTimeoutThreshold() <= 0) {
                return;
            }
            /**step4: 获取策略超时时间阈值，进行与业务耗时比较，如果超时，则超时计数器+1*/
            long timeoutThreshold = strategy.getTimeoutThreshold();
            if (timeoutThreshold < consumerTime) {
                Long startTime = getDowngradeStartTime();
                /**step5:已超时，超时计数器+1*/
                powerfulCounterService.timeoutAddAndGet(point, startTime);
            }
        } catch (Exception ex) {
            log.error("CommondDowngradeClient downgradeFinally point : {} error:{}", point, ex);
        } finally {
            /**step6:开始时间置为null，此次降级结束*/
            setDowngradeStartTime(null);
        }
    }

    protected void setDowngradeStartTime(Long time) {
        downgrateStartTime.set(time);
    }

    protected Long getDowngradeStartTime() {
        return downgrateStartTime.get();
    }

    /**
     * 记录降级的开始时间
     *
     * @param time 开始时间
     * @return: void
     * @author: xl
     * @date: 2021/6/28
     **/
    protected void setDowngradeStartTime(long time) {
        downgrateStartTime.set(time);
    }

    /**
     * 降级判断，是否需要降级处理
     *
     * @param point                降级点名称
     * @param visitCount           （当前滑动周期内）访问量调用数
     * @param curSecondVisitCount  当前秒的访问量
     * @param lastSecondVisitCount 前1秒的访问量
     * @param concurrentAcquire    并发限制结果
     * @param exceptionCount       （当前滑动周期内）异常数量
     * @param timeoutCount         （当前滑动周期内）超时数量
     * @param takeTokenBucketNum   当前秒的令牌桶计数器
     * @param downgrateCount       （当前滑动周期内）降级次数
     * @param time                 当前时间 毫秒数
     * @return: boolean 是否需要降级，true-是，false-否
     * @author: xl
     * @date: 2021/6/28
     **/
    private boolean checkDowngrate(String point, long visitCount, long curSecondVisitCount, long lastSecondVisitCount,
                                   boolean concurrentAcquire, long exceptionCount, long timeoutCount, long takeTokenBucketNum,
                                   long downgrateCount, long time) {

        /**step1：获取降级点的策略*/
        Strategy strategy = StrategyService.getInstance().getStrategy(point);

        /**没配置策略，或者降级比例小于等于0，表示不需要降级*/
        if (strategy == null || strategy.getDowngradeRate() <= 0) {
            return false;
        }

        DowngradeActionType downgradeActionType = null;
        /**step2: 校验当前降级点在当前时间内 是否需要降级延迟*/
        if (!DowngrateDelayService.getInstance().isDowngrateDelay(point, time)) {
            /**构建执行策略检查的参数*/
            CheckData checkData = CheckData.builder()
                    .point(point)
                    .time(time)
                    .visitCount(visitCount)
                    .curSecondVisitCount(curSecondVisitCount)
                    .lastSecondVisitCount(lastSecondVisitCount)
                    .concurrentAcquire(concurrentAcquire)
                    .exceptionCount(exceptionCount)
                    .timeoutCount(timeoutCount)
                    .takeTokenBucketNum(takeTokenBucketNum)
                    .downgradeCount(downgrateCount).build();
            /**step3:执行策略链，如果通过啦策略链中的所有规则，则不需要降级*/
            if ((downgradeActionType = strategyExecutor.execute(checkData, strategy)) == null) {
                return false;
            }
            /**校验是否开启啦熔断，开启熔断，则拒绝请求，进行熔断*/
            if (downgradeActionType == DowngradeActionType.FUSE) {
                return true;
            }

            /**step4:如果访问量超过阈值时，需重置降级延迟时间，降级延迟开始*/
            DowngrateDelayService.getInstance().resetExpireTime(point, time);
        } else {
            /**step3: 如果需要降级延迟，判断 此次请求是否时降级延迟的重试请求，是的话 返回，否的话 继续执行*/
            if (DowngrateDelayService.getInstance().retryChoice(point, time)) {
                log.info("CommondDowngradeClient 本次请求是降级延迟的重试请求:" + point);
                return false;
            }
        }

        /**step 5: 走到这里 表示需要进行降级，则进行降级判断*/
        boolean needDowngrate;
        /**校验降级比例 大于等于100 表示百分之百降级*/
        if (strategy.getDowngradeRate() >= 100) {
            needDowngrate = true;
        } else {
            /**随机降级*/
            needDowngrate = ThreadLocalRandom.current().nextInt(100) < strategy.getDowngradeRate();
        }
        if (needDowngrate) {
            /**降级计数器+1*/
            addDowngrateCount(point, time, downgradeActionType);
        }
        return needDowngrate;
    }

    public void addDowngrateCount(String point, long time, DowngradeActionType downgradeActionType) {

        /**降级次数+1*/
        powerfulCounterService.downgrateAddAndGet(point, time);

        /**触发降级通知*/
        DowngrateActionNotify.notify(point, downgradeActionType, new Date());
    }


}
