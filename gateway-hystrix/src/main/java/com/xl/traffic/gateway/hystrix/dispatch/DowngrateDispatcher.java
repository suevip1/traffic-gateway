package com.xl.traffic.gateway.hystrix.dispatch;

import com.xl.traffic.gateway.hystrix.AbstractDowngradeClient;
import com.xl.traffic.gateway.hystrix.XLDowngrateClientFactory;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

public class DowngrateDispatcher {


    private static ThreadPoolExecutor dispatcherExecutor = new ThreadPoolExecutor(50, 100, 1000, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(100000));


    private static class InstanceHolder {
        public static final DowngrateDispatcher instance = new DowngrateDispatcher();
    }

    public static DowngrateDispatcher getInstance() {
        return DowngrateDispatcher.InstanceHolder.instance;
    }

    /**
     * key=appGroup- list{appNames}
     */
    private static ConcurrentHashMap<String, CopyOnWriteArrayList<AbstractDowngradeClient>> appGroupsClientMap = new ConcurrentHashMap<>();


    /**
     * 添加应用组下的应用app, 创建appGroupName appName的降级客户端
     * <p>
     * //todo admin server 会通知
     *
     * @param groupName app应用组
     * @param appName   app应用名称
     * @return: void
     * @author: xl
     * @date: 2021/7/2
     **/
    public void createAppNameDowngradeClient(String groupName, String appName) {
        CopyOnWriteArrayList<AbstractDowngradeClient> pullAndPushServiceList = appGroupsClientMap.get(groupName);
        if (CollectionUtils.isEmpty(pullAndPushServiceList)) {
            pullAndPushServiceList = new CopyOnWriteArrayList<>();
        }
        boolean exist = pullAndPushServiceList.stream().filter(pullAndPushService -> pullAndPushService.getAppName().equals(appName)).findAny().isPresent();
        if (!exist) {
            pullAndPushServiceList.add(XLDowngrateClientFactory.getOrCreateSdsClient(groupName, appName));
            /**创建降级客户端*/
            appGroupsClientMap.put(groupName, pullAndPushServiceList);
        }
    }

    /**
     * 获取应用组下的应用策略from admin server
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/7/2
     **/
    public void dispatcherGroupUpdatePointStrategy() {

        for (Map.Entry<String, CopyOnWriteArrayList<AbstractDowngradeClient>> entry : appGroupsClientMap.entrySet()) {
            for (AbstractDowngradeClient client : entry.getValue()) {
                dispatcherExecutor.submit(() -> {
                    client.updatePointStrategyFromAdminServer();
                });
            }
        }
    }


    /**
     * 获取应用组下的应用策略from admin server
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/7/2
     **/
    public void dispatcherGroupPushDowngrateData() {
        for (Map.Entry<String, CopyOnWriteArrayList<AbstractDowngradeClient>> entry : appGroupsClientMap.entrySet()) {
            for (AbstractDowngradeClient client : entry.getValue()) {
                dispatcherExecutor.submit(() -> {
                    client.pushDowngrateData2Admin();
                });
            }
        }
    }


    /**
     * 根据appGroupName+appName获取降级客户端
     * 获取应用组下的某个应用的降级客户端
     *
     * @param appGroupName app应用组
     * @param appName      app应用名称
     * @return: com.xl.traffic.gateway.hystrix.AbstractDowngradeClient
     * @author: xl
     * @date: 2021/7/5
     **/
    public AbstractDowngradeClient getCommondDowngradeClientInstance(String appGroupName, String appName) {
        return getAbstractDowngradeClient(appGroupName, appName).get();
    }


    /**
     * 根据appGroupName+appName获取降级客户端
     *
     * @param appGroupName
     * @param appName
     * @return: java.util.Optional<com.xl.traffic.gateway.hystrix.AbstractDowngradeClient>
     * @author: xl
     * @date: 2021/7/5
     **/
    public Optional<AbstractDowngradeClient> getAbstractDowngradeClient(String appGroupName, String appName) {
        CopyOnWriteArrayList<AbstractDowngradeClient> abstractDowngradeClients = appGroupsClientMap.get(appGroupName);
        return abstractDowngradeClients.stream().filter(pullAndPushService -> pullAndPushService.getAppName().equals(appName)).findAny();

    }

}
