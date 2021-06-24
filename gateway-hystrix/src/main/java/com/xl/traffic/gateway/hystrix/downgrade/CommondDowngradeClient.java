package com.xl.traffic.gateway.hystrix.downgrade;

import com.xl.traffic.gateway.hystrix.AbstractGatewayDowngradeClient;

/**
 * downgrade服务降级客户端执行
 *
 * @author: xl
 * @date: 2021/6/24
 **/
public class CommondDowngradeClient extends AbstractGatewayDowngradeClient {


    public CommondDowngradeClient(String appGroupName, String appName) {
        super(appGroupName, appName);
    }

    /**
     * 服务降级入口
     *
     * @param point 降级点名称，每个应用内必须唯一
     * @return: boolean false-不需要降级，正常访问；true-需要降级
     * @author: xl
     * @date: 2021/6/24
     **/
    @Override
    public boolean shouldDowngrade(String point) {
        return false;
    }

    /**
     * 根据SdsDowngradePointFactory配置的异常列表，来判断是否增加异常量访问计数器
     * <p>
     * 注意：用户可以通过 {@see SdsPointStrategyConfig#setDowngradeExceptions(String, java.util.List)}
     * 和  {@see SdsPointStrategyConfig#setDowngradeExceptExceptions(String, java.util.List)}
     * 来设置失败异常列表和排除异常列表。
     *
     * @param point     降级点
     * @param throwable 如果exception为null，则直接进行异常量计数器+1
     * @return 当前异常数
     */
    @Override
    public void exceptionSign(String point, Throwable throwable) {

    }

    /**
     * 降级出口
     * 该方法在finally语句块中填写，必须调用，否则会导致某些资源没有被释放！
     *
     * @param point 降级点名称
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    @Override
    public void downgradeFinally(String point) {

    }
}
