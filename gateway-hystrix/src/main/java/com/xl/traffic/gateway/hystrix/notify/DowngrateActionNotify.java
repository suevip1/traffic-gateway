package com.xl.traffic.gateway.hystrix.notify;

import com.xl.traffic.gateway.hystrix.enums.DowngradeStrategyType;
import com.xl.traffic.gateway.hystrix.notify.listener.DowngrateActionListener;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.*;

/**
 * 降级通知类
 *
 * @author: xl
 * @date: 2021/6/28
 **/
@Slf4j
public class DowngrateActionNotify {


    /**
     * 降级通知线程池
     */
    private final static ExecutorService notifyPool = new ThreadPoolExecutor(10, 10,
            1L,
            TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(1000),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "DowngradeActionNotify");
                    thread.setDaemon(true);
                    return thread;
                }
            },
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    log.warn("SdsDowngradeActionNotify notifyPool已经满了，只能将该降级Action时间丢弃。");
                }
            }
    );
    /**
     * 监听器列表
     */
    private final static CopyOnWriteArrayList<DowngrateActionListener> listeners = new CopyOnWriteArrayList<>();


    public DowngrateActionNotify() {
    }


    /**
     * 添加一个降级动作的监听器
     *
     * @param downgradeActionListener
     * @return
     */
    public static boolean addDowngradeActionListener(DowngrateActionListener downgradeActionListener) {
        if (downgradeActionListener == null) {
            return false;
        }

        if (listeners.size() > 100) {
            log.warn("SdsDowngradeActionNotify#addDowngradeActionListener 难道你在死循环调用这个方法？");
            return false;
        }

        return listeners.add(downgradeActionListener);
    }


    /**
     * 降级通知
     *
     * @param point               降级点名称
     * @param downgradeStrategyType 降级触发类型
     * @param now                 当前时间
     * @return: void
     * @author: xl
     * @date: 2021/6/28
     **/
    public static void notify(final String point, final DowngradeStrategyType downgradeStrategyType, final Date now) {
        notifyPool.execute(() -> {
            for (DowngrateActionListener listener : listeners) {
                listener.downgrateAction(point, downgradeStrategyType, now);
            }
        });
    }
}
