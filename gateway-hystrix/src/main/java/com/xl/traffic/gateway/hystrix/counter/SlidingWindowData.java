package com.xl.traffic.gateway.hystrix.counter;

import com.xl.traffic.gateway.core.utils.DateUtils;
import com.xl.traffic.gateway.hystrix.constant.DowngradeConstant;
import com.xl.traffic.gateway.hystrix.model.VisitValue;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLongArray;

/**
 * 采用滑动窗口来进行数据滚动统计，让统计数据更准确更自然
 * 滑动窗口内部由固定的桶来组成，每个桶代表每秒的统计数据
 * <p>
 * 完整周期(WholeCycle ): 包含桶index0-9或者10-19或者20-29这10个桶的周期就是完整的周期。上传统计和降级的数据都来自一个完整的周期
 * 滑动周期(SlidingCycle): 从当前时间算出的当前桶index，然后往前推9个桶，这10个桶组成的周期就是滑动周期。判断要不要降级是根据当前的滑动周期的数据来的。
 * </p>
 *
 * @author: xl
 * @date: 2021/6/24
 **/
@Slf4j
public class SlidingWindowData extends AbstractCycleData {

    /**
     * 滑动窗口周期数
     */
    protected int cycleNum;

    /**
     * 滑动窗口包含桶的数量
     */
    protected int cycleBucketNum;
    /**
     * 一个桶的时间宽度，单位秒
     */
    protected int bucketTimeSecond;

    /**
     * 周期内总的桶数
     */
    protected int bucketSize;

    /**
     * 用来存储{@link com.xl.traffic.gateway.hystrix.constant.DowngradeConstant#CYCLE_NUM}个完整周期的数据
     */
    protected AtomicLongArray bucketArray;


    public SlidingWindowData(int cycleNum, int cycleBucketNum, int bucketTimeSecond) {
        this.cycleNum = cycleNum;
        this.cycleBucketNum = cycleBucketNum;
        this.bucketTimeSecond = bucketTimeSecond;

        this.bucketSize = cycleNum * cycleBucketNum;
        this.bucketArray = new AtomicLongArray(bucketSize);
    }

    /**
     * 默认滑动窗口周期数：3，滑动窗口桶的数量是10，桶的时间宽度是 1s
     */
    public SlidingWindowData() {
        this(DowngradeConstant.CYCLE_NUM, DowngradeConstant.CYCLE_BUCKET_NUM, 1);
    }

    @Override
    public VisitValue incrementAndGet(long time) {
        //获取当前时间桶所在的数组索引
        int bucketIndex = getBucketIndexByTime(time);
        //需要将该周期的所有秒数据统计出来再返回
        long curSecondValue = bucketArray.incrementAndGet(bucketIndex);
        long slidingCycleValue = curSecondValue;
//        for () {
//
//        }

        return null;
    }

    @Override
    public void setBucketValue(long time, long value) {

    }

    @Override
    public long getBucketValue(long time) {
        return 0;
    }

    @Override
    public long getCurSlidingCycleValue(long time) {
        return 0;
    }

    @Override
    public long getLastWholeCycleValue(long time) {
        return 0;
    }

    @Override
    public void clearNextCycleValue(long time) {

    }

    /**
     * 获取当前时间桶(秒)所在的数组索引
     *
     * @param time 当前时间桶 单位s
     * @return: int
     * @author: xl
     * @date: 2021/6/24
     **/
    private int getBucketIndexByTime(long time) {
        return (int) (DateUtils.getSecond(time) / bucketTimeSecond) % bucketSize;
    }

}
