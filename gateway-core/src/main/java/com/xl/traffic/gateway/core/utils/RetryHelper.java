package com.xl.traffic.gateway.core.utils;

import com.xl.traffic.gateway.core.function.CacheFunctionWithReturn;
import com.xl.traffic.gateway.core.function.CacheFunctionWithoutReturn;

/**
 * @author xuliang
 * @version 1.0
 * @project bw-server
 * @description
 * @date 2023/9/18 19:47:01
 */
public class RetryHelper {


    /**
     * @param retryCount
     * @param interval              间隔，单位ms
     * @param functionWithoutReturn
     * @return void
     * @description 重试
     * @author xuliang
     * @date 2023/9/18 19:52:45
     */
    public static void retry(Long retryCount, Long interval, CacheFunctionWithoutReturn functionWithoutReturn) {
        retryCount--;
        if (retryCount < 0) {
            return;
        }
        try {
            functionWithoutReturn.invokeMethod();
        } catch (Exception exception) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            retry(retryCount, interval, functionWithoutReturn);
        }
    }


    /**
     * @param retryCount
     * @param interval                间隔，单位ms
     * @param cacheFunctionWithReturn
     * @return void
     * @description 重试
     * @author xuliang
     * @date 2023/9/18 19:52:45
     */
    public static <T> T retryWithReturn(Long retryCount, Long interval, CacheFunctionWithReturn<T> cacheFunctionWithReturn) {
        retryCount--;
        if (retryCount < 0) {
            return null;
        }
        try {
            return cacheFunctionWithReturn.invokeMethod();
        } catch (Exception exception) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            retryWithReturn(retryCount, interval, cacheFunctionWithReturn);
        }
        return null;
    }


}
