package com.xl.traffic.gateway.hystrix.service;

import com.xl.traffic.gateway.core.counter.AbstractCycleData;
import com.xl.traffic.gateway.core.counter.SlidingWindowData;
import com.xl.traffic.gateway.hystrix.counter.ConcurrentData;
import com.xl.traffic.gateway.hystrix.counter.TokenBucketData;
import com.xl.traffic.gateway.hystrix.dispatch.DowngrateDispatcher;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

import static com.xl.traffic.gateway.hystrix.constant.DowngradeConstant.BUCKET_TIME;
import static com.xl.traffic.gateway.hystrix.constant.DowngradeConstant.CYCLE_BUCKET_NUM;

/**
 * 周期数据统计服务
 *
 * @author: xl
 * @date: 2021/6/25
 **/
public class CycleDataService {


    /**
     * 所有周期的统计数据
     */
    private final static CopyOnWriteArrayList<AbstractCycleData> allCycleTimeData = new CopyOnWriteArrayList<>();


    /**
     * 负责清理下一个周期的数据，并上传上一个周期的数据定时任务线程池
     */
    private final static ScheduledExecutorService cleanAndUploadExecutor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread("CleanAndUploadThread");
        }
    });

    /**
     * 从服务端拉取最新降级点策略配置信息定时任务线程池 默认每5s执行一次 //todo 这块可以考虑 admin变更时，主动推送过来，减少IO
     */
    private final static ScheduledExecutorService pullPointStrategyExecutor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "SdsPullPointStrategyThread");
        }
    });

    /**
     * 拉取最新策略开关
     * true-开启，false-关闭
     */
    @Getter
    @Setter
    private static volatile boolean pullPointStrategySwitch = true;


    /**
     * 向admin server 发送统计数据开关
     * true-开启，false-关闭
     */
    @Getter
    @Setter
    private static volatile boolean uploadDataSwitch = true;

    static {

        //todo 注意，这里不使用scheduleAtFixedRate，因为从长期角度来讲，scheduleAtFixedRate没有每次任务执完去计算下次执行时间准
        cleanAndUploadExecutor.schedule(new CycleClearAndUploadTask(), calDistanceNextExecuteTime(), TimeUnit.MILLISECONDS);

        /**每1h拉取一次配置*/
        /**因为有一种极端可能，假设在admin server端 新增/修改/删除 啦一个降级点，但是当点击提交的时候，
         * 此时admin server 突然挂啦，这时就会造成这个降级点永远都同步不过来，除非gateway server发生变更, 所以需要加一个保障。*/
        pullPointStrategyExecutor.scheduleAtFixedRate(new CyclePullPointStrategyTask(), 0, 1, TimeUnit.HOURS);
    }

    /**
     * 新建一个周期数据对象
     *
     * @param
     * @return: com.xl.traffic.gateway.hystrix.counter.AbstractCycleData
     * @author: xl
     * @date: 2021/6/25
     **/
    public static AbstractCycleData createCycleData() {
        AbstractCycleData abstractCycleData = new SlidingWindowData();
        allCycleTimeData.add(abstractCycleData);
        return abstractCycleData;
    }

    /**
     * 新建一个并发周期数据对象
     *
     * @param
     * @return: com.xl.traffic.gateway.hystrix.counter.AbstractCycleData
     * @author: xl
     * @date: 2021/6/25
     **/
    public static ConcurrentData createConcurrentCycleData() {
        ConcurrentData concurrentData = new ConcurrentData();
        //获取并发周期的计数器统计对象
        AbstractCycleData abstractCycleData = concurrentData.getCycleMaxConcurrentData();
        allCycleTimeData.add(abstractCycleData);
        return concurrentData;
    }

    /**
     * 新建一个令牌桶的周期数据对象
     *
     * @param
     * @return: com.xl.traffic.gateway.hystrix.counter.AbstractCycleData
     * @author: xl
     * @date: 2021/6/25
     **/
    public static TokenBucketData createTokenBucketCycleData() {
        TokenBucketData tokenBucketData = new TokenBucketData();
        //获取令牌桶的计数器统计对象
        AbstractCycleData abstractCycleData = tokenBucketData.getCycletokenBucketData();
        allCycleTimeData.add(abstractCycleData);
        return tokenBucketData;
    }


    /**
     * 将上一统计周期的桶数据汇总写入CycleTimeData#lastMinuteTotalCount，并将下一统计周期的数据清零
     * 为了保证正确性，该任务应该在每次统计周期中间来执行
     */
    @Slf4j
    private static class CycleClearAndUploadTask implements Runnable {

        @Override
        public void run() {
            try {
                long now = System.currentTimeMillis();
                /**
                 * 在每10秒的第5秒，即第15s会开始清理所有滑动周期内CycleTimeData下一周期的数据
                 */
                for (AbstractCycleData allCycleTimeDatum : allCycleTimeData) {
                    if (allCycleTimeDatum == null) {
                        continue;
                    }
                    /**
                     * 清理下一周期的所有数据
                     */
                    allCycleTimeDatum.clearNextCycleValue(now);
                }
                if (uploadDataSwitch) {
                    /**
                     * 执行上传周期统计数据 2 adminServer
                     */
                    DowngrateDispatcher.getInstance().dispatcherGroupPushDowngrateData();
                }
                /**
                 * 这里在计算下一个任务的执行时间点
                 * 因为对时间的精度要求比较高，所以不能用固定的周期方法
                 * 下一个清理任务有当前清理任务执行完成后算出来，长期来看，精准度会很高
                 * */
                cleanAndUploadExecutor.schedule(this, calDistanceNextExecuteTime(), TimeUnit.MILLISECONDS);
            } catch (Exception ex) {
                log.error("CycleClearAndUploadTask#run has exception:" + ex.getMessage(), ex);


            }
        }
    }


    /**
     * 周期性（每1h）检查一次，长时间未做更新需要 去服务端pull最新的降级点策略
     */
    private static class CyclePullPointStrategyTask implements Runnable {
        @Override
        public void run() {
            if (!pullPointStrategySwitch) {
                return;
            }
            /**执行拉取所有降级点策略的任务*/
            PullAndPushService.getInstance().initAllHystrixPointStrategyFromAdminServer();
        }
    }


    /**
     * 计算下一次任务执行的时间点
     * 为了保证正确性，{@link CycleClearAndUploadTask}任务应该在每次统计周期中间来执行，
     * 例如{@link com.xl.traffic.gateway.hystrix.constant.DowngradeConstant#CYCLE_BUCKET_NUM} 为10，
     * {@link com.xl.traffic.gateway.hystrix.constant.DowngradeConstant#BUCKET_TIME} 为1秒，
     * 那么{@link CycleClearAndUploadTask}应该在每分钟的5秒、15秒、25秒、35秒等时间来执行
     *
     * @param
     * @return: long
     * @author: xl
     * @date: 2021/6/28
     **/
    public static long calDistanceNextExecuteTime() {
        long cycleTime = CYCLE_BUCKET_NUM * BUCKET_TIME * 1000;
        long now = System.currentTimeMillis();
        return (now - now % cycleTime) + cycleTime / 2;
    }
}
