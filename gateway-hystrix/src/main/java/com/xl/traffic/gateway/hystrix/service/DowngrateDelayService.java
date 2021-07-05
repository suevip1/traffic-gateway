package com.xl.traffic.gateway.hystrix.service;


import com.xl.traffic.gateway.core.utils.AssertUtil;
import com.xl.traffic.gateway.core.utils.DateUtils;
import com.xl.traffic.gateway.hystrix.constant.DowngradeConstant;
import com.xl.traffic.gateway.hystrix.model.Strategy;
import com.xl.traffic.gateway.hystrix.utils.CacheKeyUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.security.DenyAll;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 降级延迟服务类
 *
 * @author: xl
 * @date: 2021/6/25
 **/
@Slf4j
public class DowngrateDelayService {

    private final static DowngrateDelayService downgrateDelayService = new DowngrateDelayService();


    public DowngrateDelayService() {
    }

    public static DowngrateDelayService getInstance() {
        return downgrateDelayService;
    }


    /**
     * key-降级点，value-延迟时间器
     * //todo 改变一下存储结构，key=appGroupName+appName+point
     */
    private ConcurrentHashMap<String, DelayTimer> pointDelayMap = new ConcurrentHashMap<>();


    private static ThreadLocal<Map<String, Boolean>> pointRetry = new ThreadLocal<Map<String, Boolean>>() {
        @Override
        protected Map<String, Boolean> initialValue() {
            return new HashMap<>();
        }
    };


    /**
     * 新增一个降级点延迟
     *
     * @param appGroupName 应用组
     * @param appName      应用
     * @param point        降级点
     * @param delayTime    降级延迟时间
     * @return: void
     * @author: xl
     * @date: 2021/7/5
     **/
    public void addPointDelay(String appGroupName, String appName, String point, long delayTime) {
        addPointDelay(appGroupName, appName, point, delayTime, -1);
    }


    /**
     * 新增一个降级点延迟
     *
     * @param appGroupName  应用组
     * @param appName       应用
     * @param point         降级点
     * @param delayTime     降级延迟时间
     * @param retryInterval 降级重试间隔
     * @return: void
     * @author: xl
     * @date: 2021/7/5
     **/
    public void addPointDelay(String appGroupName, String appName, String point, long delayTime, long retryInterval) {
        DelayTimer delayTimer = new DelayTimer(delayTime, retryInterval);
        delayTimer = pointDelayMap.putIfAbsent(CacheKeyUtil.getAppGroupWithAppNameWithPoint(appGroupName, appName, point), delayTimer);
        if (delayTimer == null) {
            log.info("DowngrateDelayService 新增一个降级点延迟：" + point + "-" + delayTimer);
        } else {
            log.info("DowngrateDelayService 新增一个降级点延迟失败，因为降级点已经存在：" + point + "-" + delayTimer);
        }
    }


    /**
     * 根据最新的策略来添加/更新所有的降级延迟信息
     *
     * @param strategyMap 策略map
     * @return: void
     * @author: xl
     * @date: 2021/6/25
     **/
    public void addOrUpdatePointDelay(String appGroupName, String appName,ConcurrentHashMap<String, Strategy> strategyMap) {
        for (Map.Entry<String, Strategy> entry : strategyMap.entrySet()) {
            Strategy strategy = entry.getValue();
            String point = entry.getKey();
            String key=CacheKeyUtil.getAppGroupWithAppNameWithPoint(appGroupName,appName,point);
            /**校验降级延迟时间是否大于0，如果小于0表示降级策略中没有这个特性*/
            if (strategy.getDelayTime() <= 0) {
                continue;
            }
            DelayTimer oldDelayTimer = pointDelayMap.get(key);
            if (null == oldDelayTimer) {
                //新增一个降级延迟点
                DelayTimer delayTimer = new DelayTimer(strategy.getDelayTime(), strategy.getRetryInterval());
                pointDelayMap.put(key, delayTimer);
            } else {
                //如果之前有延迟策略配置，那么久直接做更新
                oldDelayTimer.update(strategy.getDelayTime(), strategy.getRetryInterval());
                pointDelayMap.put(key, oldDelayTimer);
            }
        }
    }

    /**
     * 根据最新的策略来添加/更新单个的降级延迟信息
     *
     * @param appGroupName 应用组
     * @param appName      应用
     * @param point        降级点
     * @param strategy     策略
     * @return: void
     * @author: xl
     * @date: 2021/7/5
     **/
    public void addOrUpdatePointDelayByPoint(String appGroupName, String appName, String point, Strategy strategy) {

        String key = CacheKeyUtil.getAppGroupWithAppNameWithPoint(appGroupName, appName, point);
        /**校验降级延迟时间是否大于0，如果小于0表示降级策略中没有这个特性*/
        if (strategy.getDelayTime() <= 0) {
            return;
        }
        DelayTimer oldDelayTimer = pointDelayMap.get(key);
        if (null == oldDelayTimer) {
            //新增一个降级延迟点
            DelayTimer delayTimer = new DelayTimer(strategy.getDelayTime(), strategy.getRetryInterval());
            pointDelayMap.put(key, delayTimer);
        } else {
            //如果之前有延迟策略配置，那么久直接做更新
            oldDelayTimer.update(strategy.getDelayTime(), strategy.getRetryInterval());
            pointDelayMap.put(key, oldDelayTimer);
        }
    }


    /**
     * 该降级点在该段时间是否应该延迟降级
     *
     * @param appGroupName 应用组
     * @param appName      应用
     * @param point        降级点
     * @param time         降级时间
     * @return: boolean
     * @author: xl
     * @date: 2021/7/5
     **/
    public boolean isDowngrateDelay(String appGroupName, String appName, String point, long time) {
        DelayTimer delayTimer = pointDelayMap.get(CacheKeyUtil.getAppGroupWithAppNameWithPoint(appGroupName, appName, point));
        if (delayTimer == null) {
            return false;
        }
        return delayTimer.isDowngrateEffect(time);
    }

    /**
     * 降级延迟中的重试请求判断
     *
     * @param appGroupName 应用组
     * @param appName      应用
     * @param point        降级点
     * @param now          当前时间
     * @return: boolean
     * @author: xl
     * @date: 2021/7/5
     **/
    public boolean retryChoice(String appGroupName, String appName, String point, long now) {
        DelayTimer delayTimer = pointDelayMap.get(CacheKeyUtil.getAppGroupWithAppNameWithPoint(appGroupName, appName, point));
        if (delayTimer == null) {
            return false;
        }
        return delayTimer.canRetry(appGroupName, appName, point, now);
    }

    /**
     * 重设降级延迟时间
     *
     * @param appGroupName 应用组
     * @param appName      应用
     * @param point        降级点
     * @param curDate      当前时间
     * @return: void
     * @author: xl
     * @date: 2021/7/5
     **/
    public void resetExpireTime(String appGroupName, String appName, String point, long curDate) {
        DelayTimer delayTimer = pointDelayMap.get(CacheKeyUtil.getAppGroupWithAppNameWithPoint(appGroupName, appName, point));
        if (null != delayTimer) {
            delayTimer.resetDelayTime(curDate);
            delayTimer.resetRetryTime(curDate);
            delayTimer.resetRetryTimes();
        }
    }

    /**
     * 重试失败，继续降级延迟
     *
     * @param appGroupName 应用组
     * @param appName      应用
     * @param point        降级点
     * @param time         当前时间
     * @return: void
     * @author: xl
     * @date: 2021/7/5
     **/

    public void continueDowngrateDelay(String appGroupName, String appName, String point, long time) {
        String key = CacheKeyUtil.getAppGroupWithAppNameWithPoint(appGroupName, appName, point);
        Boolean isRetry = pointRetry.get().get(key);
        if (null == isRetry) {
            pointRetry.get().put(key, false);
            return;
        }
        /**step1：校验重试是否进行过*/
        if (isRetry) {
            DelayTimer delayTimer = pointDelayMap.get(key);
            if (null != delayTimer) {
                /**step2:重设重试时间，重试次数*/
                delayTimer.resetRetryTime(time);
                delayTimer.resetRetryTimes();
            }
            pointRetry.get().put(key, false);
        }
    }


    /**
     * 延迟时间器
     */
    @Data
    static class DelayTimer {
        /**
         * 降级延迟时间
         */
        private AtomicLong delayTime;
        /**
         * 降级延迟期间重试时间周期，默认为1
         */
        private AtomicLong retryInterval;
        /**
         * 本次过期时间
         */
        private transient AtomicLong expireTime = new AtomicLong();
        /**
         * 本次重试过期时间
         */
        private transient AtomicLong retryExpireTime = new AtomicLong();

        /**
         * 当前降级延迟期间内降级重试时间段内还剩的重试次数
         */
        private transient AtomicLong currRetryTimes = new AtomicLong();


        public DelayTimer(long delayTime, long retryInterval) {
            checkParam(delayTime, retryInterval);
            this.delayTime = new AtomicLong(delayTime);
            this.retryInterval = new AtomicLong(retryInterval);

            init();
        }


        private void init() {
            long now = System.currentTimeMillis();
            expireTime.set(now);
            retryExpireTime.set(now);
        }

        /**
         * 更新降级延迟时间，重试时间间隔
         *
         * @param delayTime
         * @param retryInterval
         * @return: void
         * @author: xl
         * @date: 2021/6/25
         **/
        public void update(long delayTime, long retryInterval) {
            checkParam(delayTime, retryInterval);
            this.delayTime = new AtomicLong(delayTime);
            this.retryInterval = new AtomicLong(retryInterval);
        }

        /**
         * 参数校验
         *
         * @param delayTime     延迟时间
         * @param retryInterval 延迟重试时间间隔
         * @return: void
         * @author: xl
         * @date: 2021/6/25
         **/
        private void checkParam(long delayTime, long retryInterval) {
            AssertUtil.greaterThanOrEqual(delayTime, DowngradeConstant.CYCLE_BUCKET_NUM * 1000, "降级延迟时间不能小于" +
                    DowngradeConstant.CYCLE_BUCKET_NUM + "秒钟！");
            if (retryInterval > 0) {
                AssertUtil.greaterThanOrEqual(retryInterval, 1000, "降级延迟重试时间不能小于1秒钟！");
            }
            if (retryInterval > delayTime) {
                throw new IllegalArgumentException("降级延迟重试时间不能大于降级延迟时间！");
            }
        }

        /**
         * 降级延迟是否有效
         *
         * @param time
         * @return: true-有效 false-无效
         * @author: xl
         * @date: 2021/6/25
         **/
        public boolean isDowngrateEffect(long time) {
            return expireTime.get() >= time;
        }

        /**
         * 是否应该进行重试
         *
         * @param point
         * @param curDate
         * @return: boolean
         * @author: xl
         * @date: 2021/6/25
         **/
        public boolean canRetry(String appGroupName, String appName, String point, long curDate) {
            /**step1：是否有重试时间*/
            if (!hasRetryTime()) {
                return false;
            }

            /**step2：只有当前时间在降级延迟时间内，并且当前时间超过重试时间，并且在重试次数还有的情况下，才进行重试*/
            if (curDate <= delayTime.get() && curDate >= retryExpireTime.get()) {

                long retryCount = currRetryTimes.get();
                if (retryCount > 0) {
                    /**step3：重试次数-1*/
                    if (currRetryTimes.compareAndSet(retryCount, retryCount - 1)) {
                        //标记重试正在进行
                        pointRetry.get().put(CacheKeyUtil.getAppGroupWithAppNameWithPoint(appGroupName, appName, point), true);
                        return true;
                    }
                }
            }
            return false;
        }


        /**
         * 是否有重试时间
         *
         * @param
         * @return: boolean
         * @author: xl
         * @date: 2021/6/28
         **/
        private boolean hasRetryTime() {
            //如果重试时间小于0，表示不重试
            return retryExpireTime.get() > 0;
        }


        /**
         * 重设过期时间
         *
         * @param curDate 过期时间
         * @return: boolean
         * @author: xl
         * @date: 2021/6/28
         **/
        public boolean resetDelayTime(long curDate) {
            long expireValue = expireTime.get();
            if (expireValue < curDate) {
                long newValue = curDate + delayTime.get();
                boolean res = expireTime.compareAndSet(expireValue, newValue);
                if (res) {
                    log.info("DowngrateDelayService 重置 【expireTime】：" + DateUtils.format(new Date(newValue), DateUtils
                            .DATE_TIME_FORMAT));
                }
                return res;
            }
            return false;
        }


        /**
         * 设置重试过期时间
         *
         * @param curDate 当前时间
         * @return: void
         * @author: xl
         * @date: 2021/6/28
         **/
        public void resetRetryTime(long curDate) {
            if (hasRetryTime()) {
                long value = curDate + retryInterval.get();
                retryExpireTime.set(value);
            }
        }

        /**
         * 设置重试次数
         *
         * @param
         * @return: void
         * @author: xl
         * @date: 2021/6/28
         **/
        public void resetRetryTimes() {
            currRetryTimes.set(1);
        }


        @Override
        public String toString() {
            return "DelayTimer [delayTime=" + delayTime + ", retryInterval="
                    + retryInterval + ", expireTime=" + expireTime
                    + ", retryExpireTime=" + retryExpireTime
                    + ", currRetryTimes=" + currRetryTimes + "]";
        }
    }


}
