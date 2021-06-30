package com.xl.traffic.gateway.hystrix;

/**
 * 服务降级的客户端api
 *
 * @author: xl
 * @date: 2021/6/24
 **/
public interface DowngradeClient {


    /**
     * 该请求是否需要降级
     *
     * @param point 降级点名称，每个应用内必须唯一
     * @return: boolean false-不需要降级，正常访问；true-需要降级
     * @author: xl
     * @date: 2021/6/24
     **/
    boolean shouldDowngrade(String point);


    /**
     * 用来标记业务处理时抛出的异常
     * 在需要使用异常率/异常量降级时需要此方法来统计异常次数
     *
     * @param point     降级点名称
     * @param throwable 业务处理时抛出的异常，如果不关系异常类型，可以传null
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    void exceptionSign(String point, Throwable throwable);


    /**
     * 退出降级逻辑
     * 该方法在finally语句块中填写，必须调用，否则会导致某些资源没有被释放！
     *
     * @param point 降级点名称
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    void downgradeFinally(String point);


}
