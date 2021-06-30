package com.xl.traffic.gateway.hystrix.service;

import com.xl.traffic.gateway.core.exception.DowngrateException;
import com.xl.traffic.gateway.core.utils.AssertUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 降级异常过滤服务类
 *
 * @author: xl
 * @date: 2021/6/28
 **/
@Slf4j
public class DowngrateExceptionService {


    public final static DowngrateExceptionService downgradeExceptionService = new DowngrateExceptionService();

    public static DowngrateExceptionService getInstance() {
        return downgradeExceptionService;
    }

    public DowngrateExceptionService() {
    }

    /**
     * key-降级点，value-降级点异常过滤
     */
    private Map<String, DowngradeException> downgradeExceptionMap = new HashMap<>();


    /**
     * 给一个降级点新增异常过滤
     *
     * @param point           降级点名称
     * @param exceptions      需要过滤异常
     * @param exceptException 不需要过滤的异常
     * @return: void
     * @author: xl
     * @date: 2021/6/28
     **/
    public void addPointException(String point, List<Class<?>> exceptions, List<Class<?>> exceptException) {

        AssertUtil.notBlack(point, "降级点不能为空！");
        if (downgradeExceptionMap.containsKey(point)) {
            log.warn("DowngrateExceptionService 新增降级点异常失败，降级点异常已经存在：" + point);
            return;
        }
        DowngradeException downgradeException = new DowngradeException(exceptions, exceptException);
        downgradeExceptionMap.put(point, downgradeException);
        log.info("DowngrateExceptionService 新增【降级点异常】成功, point:" + point + " downgradeException:"
                + downgradeException);

    }


    /**
     * 判断该异常是否是降级异常
     * 注意：如果该降级点没有配置降级异常，则所有异常默认都是失败异常
     *
     * @param point     降级点名称
     * @param exception 异常信息
     * @return
     */
    public boolean isDowngradeException(String point, Throwable exception) {
        AssertUtil.notBlack(point, "降级点不能为空！");

        if (exception == null || exception instanceof DowngrateException) {
            return false;
        }

        DowngradeException downgradeException = downgradeExceptionMap.get(point);
        if (downgradeException != null) {
            return downgradeException.isFailException(exception);
        }

        /**
         * 如果没配置降级异常，则默认所有异常都是失败异常
         */
        return true;
    }

    /**
     * 降级异常类
     */
    static class DowngradeException {
        /**
         * 抛出属于该异常类的异常属于失败
         */
        private List<Class<?>> exceptions;
        /**
         * 抛出属于该异常类的异常不属于失败
         */
        private List<Class<?>> exceptExceptions;

        public DowngradeException(List<Class<?>> exceptions, List<Class<?>> exceptExceptions) {
            this.exceptions = exceptions;
            this.exceptExceptions = exceptExceptions;
        }

        public List<Class<?>> getExceptions() {
            return exceptions;
        }

        public List<Class<?>> getExceptExceptions() {
            return exceptExceptions;
        }

        /**
         * 该异常是否是失败异常
         *
         * @param exception
         * @return
         */
        public boolean isFailException(Throwable exception) {
            if (exception == null) {
                return true;
            }

            if (exceptions.size() > 0) {
                for (Class<?> excClass : exceptions) {
                    if (excClass.isAssignableFrom(exception.getClass())) {
                        return true;
                    }
                }

                return false;
            }

            if (exceptExceptions.size() > 0) {
                for (Class<?> excClass : exceptExceptions) {
                    if (excClass.isAssignableFrom(exception.getClass())) {
                        return false;
                    }
                }

                return true;
            }

            /**
             * 如果exceptions和exceptExceptions都没设置，则默认所有异常都是失败异常
             */
            return true;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "DowngradeException [" + (exceptions != null ? "exceptions=" + exceptions + ", " : "")
                    + (exceptExceptions != null ? "exceptExceptions=" + exceptExceptions : "") + "]";
        }

    }

}
