package com.xl.traffic.gateway.core.counter;


import com.xl.traffic.gateway.core.model.VisitValue;

/**
 * 抽象周期数据类
 * 完整周期(WholeCycle ): 包含桶index0-9或者10-19或者20-29这10个桶的周期就是完整的周期。上传统计和降级的数据都来自一个完整的周期
 * 滑动周期(SlidingCycle): 从当前时间算出的当前桶index，然后往前推9个桶，这10个桶组成的周期就是滑动周期。判断要不要降级是根据当前的滑动周期的数据来的。
 *
 * @author: xl
 * @date: 2021/6/24
 **/
public abstract class AbstractCycleData {


    /**
     * 时间对应的桶值+1
     *
     * @param time
     * @return: com.xl.traffic.gateway.hystrix.model.VisitValue
     * @author: xl
     * @date: 2021/6/24
     **/

    abstract public VisitValue incrementAndGet(long time);

    /**
     * 设置对应时间的桶的值
     *
     * @param time
     * @param value
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    abstract public void setBucketValue(long time, long value);


    /**
     * 获取对应时间桶的值
     *
     * @param time
     * @return: long
     * @author: xl
     * @date: 2021/6/24
     **/
    abstract public long getBucketValue(long time);


    /**
     * 获取当前时间所处的【滑动周期】的统计值
     *
     * @param time
     * @return: long
     * @author: xl
     * @date: 2021/6/24
     **/
    abstract public long getCurSlidingCycleValue(long time);


    /**
     * 获取当前时间所处周期的上个【完整周期】所有桶的总值
     *
     * @param time
     * @return: long
     * @author: xl
     * @date: 2021/6/24
     **/
    abstract public long getLastWholeCycleValue(long time);


    /**
     * 清理当前时间所处周期的下个完整周期的数据
     *
     * @param time
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    abstract public void clearNextCycleValue(long time);


}
