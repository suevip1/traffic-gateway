package com.xl.traffic.gateway.hystrix.model;

import lombok.Data;

/**
 * 访问量
 *
 * @author: xl
 * @date: 2021/6/24
 **/
@Data
public class VisitValue {


    /**
     * 当前桶值
     */
    private long bucketValue;


    /**
     * 滑动周期值
     */
    private long slidingCycleValue;


}
