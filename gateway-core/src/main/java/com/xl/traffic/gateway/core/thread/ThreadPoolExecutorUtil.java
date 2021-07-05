package com.xl.traffic.gateway.core.thread;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 线程池工具类
 *
 * @author: xl
 * @date: 2021/7/5
 **/
@Slf4j
public class ThreadPoolExecutorUtil {

    /**
     * IO线程池 公共
     */
    @Getter
    private final static ExecutorService commonIOPool = new ThreadPoolExecutor(500, 1000,
            1L,
            TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(1000000),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "common-io-executor");
                    thread.setDaemon(true);
                    return thread;
                }
            },
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    log.warn("common-io-executor 丢弃。");
                }
            }
    );
}
