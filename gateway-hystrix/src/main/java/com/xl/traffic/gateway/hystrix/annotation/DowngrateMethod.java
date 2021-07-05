package com.xl.traffic.gateway.hystrix.annotation;

import java.lang.annotation.*;

/**
 * 服务降级方法注解
 *
 * @author: xl
 * @date: 2021/6/28
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DowngrateMethod {

    /**
     * app应用组
     */
    String appGroupName();

    /**
     * app应用
     */
    String appName();

    /**
     * 降级点
     */
    String point();

    /**
     * 异常类型
     */
    Class<?> exceptionClass() default Exception.class;


    /**
     * 出现异常后的降级方法名称，和使用注解标注的方法在同一个类中
     * fallback 指定的方法 > sds-admin 配置的策略 > 默认（抛出异常）
     */
    String fallback() default "";


}
