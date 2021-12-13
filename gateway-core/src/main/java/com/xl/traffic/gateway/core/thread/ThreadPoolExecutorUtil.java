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
     * GATEWAY_LOGIN IO线程池
     */
    @Getter
    private final static ExecutorService Gateway_Login_Pool = new ThreadPoolExecutor(500, 1000,
            1L,
            TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(1000000),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "gateway-login-io-executor");
                    thread.setDaemon(true);
                    return thread;
                }
            },
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    log.warn("gateway-login-io-executor 丢弃。");
                }
            }
    );
    /**
     * GATEWAY_LOGIN_Out IO线程池
     */
    @Getter
    private final static ExecutorService Gateway_Login_Out_Pool = new ThreadPoolExecutor(500, 1000,
            1L,
            TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(1000000),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "gateway-login-out-io-executor");
                    thread.setDaemon(true);
                    return thread;
                }
            },
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    log.warn("gateway-login-out-io-executor 丢弃。");
                }
            }
    );



    /**
     * GATEWAY_REGISTER IO线程池
     */
    @Getter
    private final static ExecutorService Gateway_Register_Pool = new ThreadPoolExecutor(500, 1000,
            1L,
            TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(1000000),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "gateway-register-io-executor");
                    thread.setDaemon(true);
                    return thread;
                }
            },
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    log.warn("gateway-register-io-executor 丢弃。");
                }
            }
    );



    /**
     * 密集型计算线程池
     */
    @Getter
    private final static ExecutorService commonCPUPool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() * 2,
            1L,
            TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(1000000),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "common-CPU-executor");
                    thread.setDaemon(true);
                    return thread;
                }
            },
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    log.warn("common-cpu-executor 丢弃。");
                }
            }
    );


}
