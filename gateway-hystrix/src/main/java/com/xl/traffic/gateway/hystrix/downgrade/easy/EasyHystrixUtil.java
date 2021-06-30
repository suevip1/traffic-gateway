package com.xl.traffic.gateway.hystrix.downgrade.easy;

import com.xl.traffic.gateway.hystrix.DowngradeClient;
import com.xl.traffic.gateway.hystrix.XLDowngrateClientFactory;
import org.springframework.util.StringUtils;

/**
 * 便捷使用降级方法
 *
 * @author: xl
 * @date: 2021/6/29
 **/
public class EasyHystrixUtil {


    /**
     * 执行有降级逻辑的业务方法，降级后将返回指定的默认值
     *
     * @param point          降级点
     * @param downgrateValue 降级后返回的默认值
     * @param bizFunction    业务方法
     * @return: T
     * @author: xl
     * @date: 2021/6/29
     **/
    public static <T> T invokeMethod(String point, T downgrateValue, BizFunction<T> bizFunction) {
        if (bizFunction == null) {
            return downgrateValue;
        }

        /**
         * 如果降级点为空，相当于没有降级功能，直接执行业务方法返回
         */
        if (StringUtils.isEmpty(point)) {
            return bizFunction.invokeBizMethod();
        }

        DowngradeClient downgradeClient = XLDowngrateClientFactory.getDowngrateClient();

        if (downgradeClient == null) {
            return bizFunction.invokeBizMethod();
        }
        /**
         * 开始执行降级
         */
        try {
            /**step1：降级入口*/
            if (downgradeClient.shouldDowngrade(point)) {
                return downgrateValue;
            }
            /**执行业务方法*/
            return bizFunction.invokeBizMethod();
        } catch (Throwable exception) {
            /**step2：标记异常点*/
            downgradeClient.exceptionSign(point, exception);
            throw exception;
        } finally {
            /**step3：降级资源释放*/
            downgradeClient.downgradeFinally(point);
        }
    }


    /**
     * 执行有降级逻辑的业务方法,适用于无返回值的场景
     *
     * @param point       降级点
     * @param bizFunction 业务方法
     * @author: xl
     * @date: 2021/6/29
     **/
    public static void invokeMethodWithoutReturn(String point, BizFunction bizFunction) {
        if (bizFunction == null) {
            return;
        }

        /**
         * 如果降级点为空，相当于没有降级功能
         */
        if (StringUtils.isEmpty(point)) {
            bizFunction.invokeBizMethod();
            return;
        }

        DowngradeClient downgradeClient = XLDowngrateClientFactory.getDowngrateClient();

        if (downgradeClient == null) {
            bizFunction.invokeBizMethod();
            return;
        }
        /**
         * 开始执行降级
         */
        try {

            /**step1：降级入口*/
            if (downgradeClient.shouldDowngrade(point)) {
                return;
            }


            /**执行业务方法*/
            bizFunction.invokeBizMethod();


        } catch (Throwable exception) {

            /**step2：标记异常点*/
            downgradeClient.exceptionSign(point, exception);
            throw exception;

        } finally {

            /**step3：降级资源释放*/
            downgradeClient.downgradeFinally(point);

        }
    }

    /**
     * 执行有降级逻辑的业务方法，降级后将执行指定的降级方法，类似于fallback
     *
     * @param point             降级点
     * @param downgrateFunction 降级后执行的方法
     * @param bizFunction       业务方法
     * @return: T
     * @author: xl
     * @date: 2021/6/29
     **/
    public static <T> T invokeDowngrateMethod(String point,
                                              DowngrateFunction<T> downgrateFunction, BizFunction<T> bizFunction) {
        if (bizFunction == null) {
            if (downgrateFunction == null) {
                throw new IllegalArgumentException("EasyHystrixUtil#invokeDowngrateMethod downgradeFunction " +
                        "and bizFunction cannot null");
            }
            return downgrateFunction.invokeDowngrateMethod();
        }

        /**
         * 如果降级点为空，相当于没有降级功能，直接执行业务方法返回
         */
        if (StringUtils.isEmpty(point)) {
            return bizFunction.invokeBizMethod();
        }

        DowngradeClient downgradeClient = XLDowngrateClientFactory.getDowngrateClient();

        if (downgradeClient == null) {
            return bizFunction.invokeBizMethod();
        }
        /**
         * 开始执行降级
         */
        try {
            /**step1：降级入口*/
            if (downgradeClient.shouldDowngrade(point)) {
                /**执行降级后的方法*/
                return downgrateFunction.invokeDowngrateMethod();
            }
            /**执行业务方法*/
            return bizFunction.invokeBizMethod();
        } catch (Throwable exception) {
            /**step2：标记异常点*/
            downgradeClient.exceptionSign(point, exception);
            throw exception;
        } finally {
            /**step3：降级资源释放*/
            downgradeClient.downgradeFinally(point);
        }
    }


    /**
     * 执行有降级逻辑的业务方法，降级后将执行指定的降级方法，类似于fallback
     *
     * @param point             降级点
     * @param downgrateFunction 降级后执行的方法
     * @param bizFunction       业务方法
     * @author: xl
     * @date: 2021/6/29
     **/
    public static void invokeDowngrateMethodWithoutReturn(String point,
                                                          DowngrateFunctionWithoutReturn downgrateFunction, BizFunction bizFunction) {
        if (bizFunction == null) {
            if (downgrateFunction == null) {
                throw new IllegalArgumentException("EasyHystrixUtil#invokeDowngrateMethod downgradeFunction " +
                        "and bizFunction cannot null");
            }
            downgrateFunction.invokeDowngrateMethod();
            return;
        }

        /**
         * 如果降级点为空，相当于没有降级功能，直接执行业务方法返回
         */
        if (StringUtils.isEmpty(point)) {
            bizFunction.invokeBizMethod();
            return;
        }

        DowngradeClient downgradeClient = XLDowngrateClientFactory.getDowngrateClient();

        if (downgradeClient == null) {
            bizFunction.invokeBizMethod();
            return;
        }
        /**
         * 开始执行降级
         */
        try {
            /**step1：降级入口*/
            if (downgradeClient.shouldDowngrade(point)) {
                /**执行降级后的方法*/
                downgrateFunction.invokeDowngrateMethod();
                return;
            }
            /**执行业务方法*/
            bizFunction.invokeBizMethod();
        } catch (Throwable exception) {
            /**step2：标记异常点*/
            downgradeClient.exceptionSign(point, exception);
            throw exception;
        } finally {
            /**step3：降级资源释放*/
            downgradeClient.downgradeFinally(point);
        }
    }


    /**
     * 一键熔断开关
     * 有时候需要做熔断功能，不需要其它限流方式，类似于一键开关，那么就可以直接使用该方法
     *
     * @param point 降级点名称
     * @return: boolean true-熔断打开，不应该执行业务逻辑，业务方直接走降级逻辑;false-熔断关闭，应该执行业务逻辑
     * @author: xl
     * @date: 2021/6/29
     **/
    public static boolean oneButtonFusingSwitch(String point) {

        if (StringUtils.isEmpty(point)) {
            return false;
        }


        DowngradeClient downgradeClient = XLDowngrateClientFactory.getDowngrateClient();

        if (downgradeClient == null) {
            return false;
        }

        try {
            if (downgradeClient.shouldDowngrade(point)) {
                return true;
            }

        } finally {
            downgradeClient.downgradeFinally(point);
        }
        return false;
    }


}
