package com.xl.traffic.gateway.core.utils;

import java.util.concurrent.TimeUnit;

public class TimeStatisticsUtil {


    private static ThreadLocal<Long> startTimeThreadLocal = new ThreadLocal<>();

    /**
     * 记录开始时间
     */
    public static void startTime() {
        startTime(System.nanoTime());
    }

    /**
     * 记录开始时间
     */
    public static void startTime(long time) {
        startTimeThreadLocal.set(time);
    }

    /**
     * 获取消耗的时间，单位毫秒
     */
    public static long getrConsumerTime() {
        Long startTime = startTimeThreadLocal.get();
        if (startTime != null) {
            return (System.nanoTime() - startTime) / 1000000;
        }
        return -1;
    }


    public static void main(String[] args) {
        long t1 = System.currentTimeMillis();
        long t2 = System.nanoTime();

        System.out.println(t1+"----"+t2);
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
        }

        System.out.println(System.currentTimeMillis() - t1);
        System.out.println(System.nanoTime() - t2);
    }


}
