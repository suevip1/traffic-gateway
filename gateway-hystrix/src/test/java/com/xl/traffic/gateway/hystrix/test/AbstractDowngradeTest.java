package com.xl.traffic.gateway.hystrix.test;

import com.xl.traffic.gateway.hystrix.DowngradeClient;
import com.xl.traffic.gateway.hystrix.XLDowngrateClientFactory;
import com.xl.traffic.gateway.hystrix.dispatch.DowngrateDispatcher;
import com.xl.traffic.gateway.hystrix.service.CycleDataService;
import org.junit.Before;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 降级单元测试基类，负责集成封装方法
 *
 * @author: xl
 * @date: 2021/6/29
 **/
public abstract class AbstractDowngradeTest {


    //业务方法是否需要抛出异常
    private static volatile boolean hasException = false;

    private ExecutorService service = null;

    static {
        //不从admin server  获取配置，完全使用本地手动配置
        CycleDataService.setPullPointStrategySwitch(false);
        CycleDataService.setUploadDataSwitch(false);
    }

    /**
     * 初始化策略
     */
    @Before
    public void init() {

        initStrategy();

        service = new ThreadPoolExecutor(getThreadNum(), getThreadNum(), 1000, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100));
    }

    /**
     * 执行线程池，并发触发降级逻辑
     *
     * @return:
     * @author: xl
     * @date: 2021/6/29
     **/
    public void executor() {
        long startTime = System.currentTimeMillis();
        try {
            /**保证从完整的第10s开始执行，增加准确性*/
            Thread.sleep(10000 - startTime % 10000 + 100);
        } catch (Exception ex) {

        }

        long i = getThreadNum();
        while (i-- > 0) {
            service.submit(new ClientThread());
        }


    }


    /**
     * 初始化策略
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/6/29
     **/
    protected abstract void initStrategy();

    /**
     * 初始化策略
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/6/29
     **/
    protected abstract DowngradeClient getDowngradeClient();


    /**
     * 应用组名称
     *
     * @param
     * @return: java.lang.String
     * @author: xl
     * @date: 2021/6/29
     **/
    protected abstract String getAppGroupName();

    /**
     * 应用名称
     *
     * @param
     * @return: java.lang.String
     * @author: xl
     * @date: 2021/6/29
     **/
    protected abstract String getApp();


    /**
     * 降级点名称
     *
     * @param
     * @return: java.lang.String
     * @author: xl
     * @date: 2021/6/29
     **/
    protected abstract String getPoint();


    /**
     * 获取执行的线程池数
     *
     * @param
     * @return: int
     * @author: xl
     * @date: 2021/6/29
     **/
    protected abstract int getThreadNum();


    /**
     * 获取每个线程执行业务的次数
     *
     * @param
     * @return: int
     * @author: xl
     * @date: 2021/6/29
     **/
    protected abstract int getExecutorTimes();


    /**
     * 业务方法执行的耗时
     *
     * @param
     * @return: long
     * @author: xl
     * @date: 2021/6/29
     **/
    protected abstract long getTakeTimes();


    /**
     * 获取结果，至少等待一个完整周期，即10s
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/6/29
     **/
    protected void waitResult() {
        sleepMillSeconds(12000);
    }

    /**
     * 睡眠阻塞时间
     *
     * @param sleepMillSeconds
     * @return: void
     * @author: xl
     * @date: 2021/6/29
     **/
    protected void sleepMillSeconds(long sleepMillSeconds) {

        try {
            TimeUnit.MILLISECONDS.sleep(sleepMillSeconds);
        } catch (InterruptedException interruptedException) {
        }
    }

    /**
     * 模拟执行业务 ,接入降级
     *
     * @return:
     * @author: xl
     * @date: 2021/6/29
     **/
    private void businessMethod() {
        try {
            /**step1:降级入口判断*/
            if (getDowngradeClient().shouldDowngrade(getPoint())) {
                return;
            }
            /**模拟正常业务执行*/
            sleepMillSeconds(getTakeTimes());

            /**如果业务逻辑设置成需要抛出异常，那么就抛出异常*/
            if (hasException) {
                throw new IllegalArgumentException("异常抛出啦！");
            }
        } catch (Throwable throwable) {
            /**step2：降级统计异常量*/
            getDowngradeClient().exceptionSign(getPoint(), throwable);
            throw throwable;
        } finally {
            /**step3:降级资源释放*/
            getDowngradeClient().downgradeFinally(getPoint());
        }
    }

    private class ClientThread implements Runnable {

        @Override
        public void run() {
            long executorTimes = getExecutorTimes();
            while (executorTimes-- > 0) {
                try {
                    //执行业务
                    businessMethod();
                } catch (Exception e) {
                }
            }
        }
    }


}
