package com.xl.traffic.gateway.hystrix.test;

import com.xl.traffic.gateway.hystrix.DowngradeClient;
import com.xl.traffic.gateway.hystrix.counter.PowerfulCycleTimeCounter;
import com.xl.traffic.gateway.hystrix.dispatch.DowngrateDispatcher;
import com.xl.traffic.gateway.hystrix.model.Strategy;
import com.xl.traffic.gateway.hystrix.service.PowerfulCounterService;
import com.xl.traffic.gateway.hystrix.service.StrategyService;
import org.junit.Test;

/**
 * 并发量降级测试
 *
 * @author: xl
 * @date: 2021/6/29
 **/
public class ConcurrentDowngrateTest extends AbstractDowngradeTest {


    /**
     * 并发量阈值
     */
    private final static int CONCURRENT_THRESHOLD = 2;


    @Test
    public void test() {


        /**
         * 实际场景：5个线程，每秒访问一次业务方法，每个线程访问10次
         * 策略：并发阈值2，降级比例：100
         * */

        executor();

        waitResult();

        //获取降级点计数器服务
        PowerfulCycleTimeCounter powerfulCycleTimeCounter = PowerfulCounterService.getInstance().getPointCounterMap().get(getPoint());

        long lastSecondDowngrateCount = powerfulCycleTimeCounter.getLastDowngrateCycleValue(System.currentTimeMillis());

        /**
         * 因为同时只允许两个线程来调用该方法，所以每次有3个线程会被降级，所以降级数量应该是30次
         */
//        Assert.assertEquals((getThreadNum() - CONCURRENT_THRESHOLD) * 10, lastCycleDowngradeValue);


        System.out.println("降级数量：" + lastSecondDowngrateCount);


    }


    @Override
    protected void initStrategy() {


        Strategy strategy = new Strategy();

        strategy.setPoint(getPoint());
        strategy.setConcurrentThreshold(CONCURRENT_THRESHOLD);

        /**
         * 重设策略
         */
        StrategyService.getInstance().updateStrategyByPoint(getAppGroupName(), getApp(), getPoint(), strategy);
    }

    @Override
    protected String getPoint() {
        return "concurrentPoint";
    }

    @Override
    protected DowngradeClient getDowngradeClient() {
        return DowngrateDispatcher.getInstance().getCommondDowngradeClientInstance(getAppGroupName(), getApp());
    }

    @Override
    protected String getAppGroupName() {
        return "concurrent_group";
    }

    @Override
    protected String getApp() {
        return "concurrent";
    }

    @Override
    protected int getThreadNum() {
        return 5;
    }

    @Override
    protected int getExecutorTimes() {
        return 10;
    }

    @Override
    protected long getTakeTimes() {
        return 950;
    }
}
