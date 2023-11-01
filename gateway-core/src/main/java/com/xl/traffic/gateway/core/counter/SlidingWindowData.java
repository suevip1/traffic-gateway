package com.xl.traffic.gateway.core.counter;

import com.xl.traffic.gateway.core.model.VisitValue;
import com.xl.traffic.gateway.core.utils.DateUtils;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLongArray;

/**
 * 采用滑动窗口来进行数据滚动统计，让统计数据更准确更自然
 * 滑动窗口内部由固定的桶来组成，每个桶代表单位时间内(例如：每秒)的统计数据
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
     * 用来存储{@link GatewayConstants#CYCLE_NUM}个完整周期的数据
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
        this(GatewayConstants.CYCLE_NUM, GatewayConstants.CYCLE_BUCKET_NUM, GatewayConstants.BUCKET_TIME);
    }

    /**
     * 时间对应的桶值+1
     *
     * @param time
     * @return: com.xl.traffic.gateway.hystrix.model.VisitValue
     * @author: xl
     * @date: 2021/6/24
     **/
    @Override
    public VisitValue incrementAndGet(long time) {
        //获取当前时间桶所在的数组索引
        int bucketIndex = getBucketIndexByTime(time);
        /**假设index：0
         * 存储该滑动时间的数据应该在第11个格子中，往前推9个格子
         * */
        //需要将当前周期[10个桶]的所有秒数据统计出来再返回
        long curSecondValue = bucketArray.incrementAndGet(bucketIndex);
        long slidingCycleValue = curSecondValue;
        for (int i = bucketIndex - cycleBucketNum + 1; i < bucketIndex; i++) {
            //往前推9个桶，获取前9个桶的总和加上当前桶的数值,组成当前滑动周期内的周期数据[10个桶]
            slidingCycleValue += bucketArray.get(switchIndex(i));
        }
        return new VisitValue(curSecondValue, slidingCycleValue);
    }

    /**
     * 设置对应时间的桶的值
     *
     * @param time
     * @param value
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    @Override
    public void setBucketValue(long time, long value) {
        int bucketIndex = getBucketIndexByTime(time);
        bucketArray.set(bucketIndex, value);
    }

    /**
     * 获取对应时间桶的值
     *
     * @param time
     * @return: long
     * @author: xl
     * @date: 2021/6/24
     **/
    @Override
    public long getBucketValue(long time) {
        int bucketIndex = getBucketIndexByTime(time);
        return bucketArray.get(bucketIndex);
    }


    /**
     * 获取当前时间所处的【滑动周期】的统计值
     *
     * @param time
     * @return: long
     * @author: xl
     * @date: 2021/6/24
     **/
    @Override
    public long getCurSlidingCycleValue(long time) {
        int bucketIndex = getBucketIndexByTime(time);
        return getSlidingCycleBucketTotalValue(bucketIndex);
    }


    /**
     * 获取当前时间所处周期的上个【完整周期】所有桶的总值
     *
     * @param time
     * @return: long
     * @author: xl
     * @date: 2021/6/24
     **/
    @Override
    public long getLastWholeCycleValue(long time) {
        int bucketIndex = getBucketIndexByTime(time);

        return getLastWholeCycleBucketTotalValue(bucketIndex);
    }

    @Override
    public void clearNextCycleValue(long time) {
        cleanNextBucketArrayValue(time);
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

    /**
     * 由于需要把桶（秒）的数组打造成环形数组，所以这里需要对index做特殊处理
     *
     * @param index
     * @return: int 桶的index
     * @author: xl
     * @date: 2021/6/25
     **/
    private int switchIndex(int index) {
        if (index > 0 && index < bucketSize) {
            //返回周期内的索引
            return index;
        } else if (index < 0) {
            return bucketSize + index;
        } else {
            return index - bucketSize;
        }
    }


    /**
     * 计算桶索引所在【滑动周期内】的所有bucket中的值得和
     *
     * @param bucketIndex 当前桶的索引
     * @return: long
     * @author: xl
     * @date: 2021/6/25
     **/
    private long getSlidingCycleBucketTotalValue(int bucketIndex) {
        //获取当前桶的滑动周期值
        long totalValue = bucketArray.get(bucketIndex);
        //往前推9个桶
        for (int i = bucketIndex - cycleBucketNum + 1; i < bucketIndex; i++) {
            totalValue += bucketArray.get(switchIndex(i));
        }
        return totalValue;
    }


    /**
     * 计算桶索引所在上一【完整周期】内的所有bucket中值得总和
     *
     * @param bucketIndex 桶索引
     * @return: long
     * @author: xl
     * @date: 2021/6/25
     **/
    private long getLastWholeCycleBucketTotalValue(int bucketIndex) {
        long total = 0;
        //获取滑动周期数=周期内的最大索引值/周期内的桶的数量值
        int cycleNum = switchIndex(bucketIndex - cycleBucketNum) / cycleBucketNum;
        //计算桶的开始索引
        int startBucketIndex = cycleNum * cycleBucketNum;
        //计算桶的结束索引
        int endBucketIndex = startBucketIndex + cycleBucketNum;

        for (int i = startBucketIndex; i < endBucketIndex; i++) {
            //这里边的值 绝对不会超过 bucketSize，因为已经被 switchIndex 处理过啦
            total += bucketArray.get(i);
        }
        return total;
    }


    /**
     * 提前将下面的桶索引的数据清零
     *
     * @param time
     * @return: void
     * @author: xl
     * @date: 2021/6/25
     **/
    private void cleanNextBucketArrayValue(long time) {

        int bucketIndex = getBucketIndexByTime(time);
        int cycleNum = switchIndex(bucketIndex + cycleBucketNum) / cycleBucketNum;
        int startBucketIndex = cycleNum * cycleBucketNum;
        int endBucketIndex = startBucketIndex + cycleBucketNum;

        for (int i = startBucketIndex; i < endBucketIndex; i++) {
            bucketArray.set(i, 0);
        }


    }


}
