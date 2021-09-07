package com.xl.traffic.gateway.hystrix.service;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.enums.MsgAppNameType;
import com.xl.traffic.gateway.core.enums.MsgCMDType;
import com.xl.traffic.gateway.core.enums.MsgGroupType;
import com.xl.traffic.gateway.core.enums.SerializeType;
import com.xl.traffic.gateway.core.gson.GSONUtil;
import com.xl.traffic.gateway.core.serialize.ISerialize;
import com.xl.traffic.gateway.core.serialize.SerializeFactory;
import com.xl.traffic.gateway.core.utils.DateUtils;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.core.utils.SnowflakeIdWorker;
import com.xl.traffic.gateway.hystrix.counter.PowerfulCycleTimeCounter;
import com.xl.traffic.gateway.hystrix.dispatch.DowngrateDispatcher;
import com.xl.traffic.gateway.hystrix.model.PushCycleData;
import com.xl.traffic.gateway.hystrix.model.PushRequest;
import com.xl.traffic.gateway.hystrix.model.PushResponse;
import com.xl.traffic.gateway.hystrix.model.Strategy;
import com.xl.traffic.gateway.hystrix.utils.BaseValidator;
import com.xl.traffic.gateway.rpc.manager.RemoteRpcClientManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.xl.traffic.gateway.hystrix.constant.DowngradeConstant.BUCKET_TIME;
import static com.xl.traffic.gateway.hystrix.constant.DowngradeConstant.CYCLE_BUCKET_NUM;

/**
 * 服务端拉取页面配置
 * 用于客户端上传数据和从服务端拉取最新的降级配置
 *
 * @author: xl
 * @date: 2021/6/28
 **/
@Slf4j
public class PullAndPushService {


    ISerialize iSerialize = SerializeFactory.getInstance().getISerialize(SerializeType.protobuf);

    private static class InstanceHolder {
        public static final PullAndPushService instance = new PullAndPushService();
    }

    public static PullAndPushService getInstance() {
        return InstanceHolder.instance;
    }


    public PullAndPushService() {

    }

    /**
     * 汇报本地的降级数据给admin
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/6/28
     **/
    public void pushDowngrateData2Admin(String appGroupName, String appName) {
        try {
            BaseValidator.baseParamValidator(appGroupName, appName);
            Date date = new Date();
            Map<String, PushCycleData> pushCycleDataMap = buildPointCycleInfo(date.getTime());
            PushRequest pushRequest = PushRequest.builder()
                    .appGroupName(appGroupName)
                    .appName(appName)
                    .hostname(AddressUtils.getHostName())

                    .ip(AddressUtils.getInnetIp())
                    .pointInfoMap(pushCycleDataMap)
                    .statisticsCycleTime(new Date(DateUtils.getLastCycleEndTime(CYCLE_BUCKET_NUM * BUCKET_TIME,
                            date.getTime())))
                    .build();
            /**构建rpc消息*/
            RpcMsg rpcMsg = new RpcMsg(MsgCMDType.UPLOAD_DOWNGRATE_DATA_CMD.getType(), MsgGroupType.GATEWAY.getType(), MsgAppNameType.GATEWAY.getType(), SnowflakeIdWorker.getInstance().nextId(),
                    iSerialize.serialize(pushRequest));
            /**通过rpc 发送给admin端*/
            RemoteRpcClientManager.getInstance().sendAsync(GatewayConstants.GATEWAY_GROUP,
                    iSerialize.serialize(rpcMsg));
        } catch (Exception exception) {
            //todo
            log.error("pushDowngrateData2Admin error:{}", exception);
        }
    }

    /**
     * 根据appGroupName，appName从admin服务端拉取最新的降级点配置信息
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/7/5
     **/
    public void updatePointStrategyFromAdminServer(String appGroupName, String appName) {
        BaseValidator.baseParamValidator(appGroupName, appName);
        try {
            /**获取所有的降级点*/
            List<String> points = PowerfulCounterService.getInstance().getPointCounterMap().keySet().stream().collect(Collectors.toList());
            PushRequest pushRequest = PushRequest.builder()
                    .appName(appName)
                    .appGroupName(appGroupName)
                    .ip(AddressUtils.getInnetIp())
                    .hostname(AddressUtils.getHostName())
                    .pointList(points)
                    .build();
            /**此处使用同步rpc，拉取最新配置*/
            byte[] res = RemoteRpcClientManager.getInstance().sendSync(GatewayConstants.GATEWAY_GROUP, iSerialize.serialize(pushRequest));
            if (res == null) {
                log.info("PullAndPushService#updatePointStrategyFromWebServer 服务端应答转JSON为null，请求参数："
                        + GSONUtil.toJson(pushRequest));
                return;
            }
            PushResponse response = iSerialize.deserialize(res, PushResponse.class);
            if (StringUtils.isNotBlank(response.getErrorMsg())) {
                log.info("PullAndPushService#updatePointStrategyFromWebServer 服务端有错误信息：" + response.getErrorMsg());
                return;
            }
            // 如果没有更新，直接返回
            if (response.getChanged() == null || !response.getChanged()) {
                log.info("PullAndPushService#updatePointStrategyFromWebServer 版本号没变，无需更新");
                return;
            }
            ConcurrentHashMap<String, Strategy> strategies = new ConcurrentHashMap<>();
            if (!CollectionUtils.isEmpty(response.getStrategies())) {
                for (Strategy strategy : response.getStrategies()) {
                    strategies.put(strategy.getPoint(), strategy);
                }
            }
            /**更新降级点信息*/
            updatePointInfo(appGroupName, appName, strategies);

        } catch (Exception exception) {
            log.error("PullAndPushService updatePointStrategyFromAdminServer is error:{}", exception);
        }
    }

    /**
     * 从admin服务端拉取最新的降级点配置信息【初始化所有】
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/6/28
     **/
    public void initAllHystrixPointStrategyFromAdminServer() {
        try {
            PushRequest pushRequest = PushRequest.builder()
                    .ip(AddressUtils.getInnetIp())
                    .hostname(AddressUtils.getHostName())
                    .build();

            /**此处使用同步rpc，拉取最新配置*/
            byte[] res = RemoteRpcClientManager.getInstance().sendSync(GatewayConstants.GATEWAY_GROUP, iSerialize.serialize(pushRequest));
            if (res == null) {
                log.info("PullAndPushService#updatePointStrategyFromWebServer 服务端应答转JSON为null，请求参数："
                        + GSONUtil.toJson(pushRequest));
                return;
            }
            List<PushResponse> responses = iSerialize.deserialize(res, List.class);
            for (PushResponse response : responses) {
                updateHystrix(response);
            }
        } catch (Exception exception) {
            log.error("PullAndPushService updatePointStrategyFromAdminServer is error:{}", exception);
        }
    }

    public void updateHystrix(PushResponse response) {
        if (StringUtils.isNotBlank(response.getErrorMsg())) {
            log.info("PullAndPushService 服务端有错误信息：" + response.getErrorMsg());
            return;
        }
        // 如果没有更新，直接返回
        if (response.getChanged() == null || !response.getChanged()) {
            log.info("PullAndPushService 版本号没变，无需更新");
            return;
        }
        ConcurrentHashMap<String, Strategy> strategies = new ConcurrentHashMap<>();
        if (!CollectionUtils.isEmpty(response.getStrategies())) {
            for (Strategy strategy : response.getStrategies()) {
                strategies.put(strategy.getPoint(), strategy);
            }
        }

        /**创建降级客户端*/
        DowngrateDispatcher.getInstance().createAppNameDowngradeClient(response.getAppGroupName(), response.getAppName());

        /**更新降级点信息*/
        updatePointInfo(response.getAppGroupName(), response.getAppName(), strategies);
    }

    public void updatePointInfo(String appGroupName, String appName, ConcurrentHashMap<String, Strategy> strategyMap) {
        /**重设降级点策略*/
        StrategyService.getInstance().updateAllStrategy(appGroupName, appName, strategyMap);

        /**
         * 重设降级点返回值
         */
        //todo 降级返回值是json格式
    }

    /**
     * 根据当前时间获取所有降级点上一完整周期的统计信息
     *
     * @param time
     * @result
     */
    private Map<String, PushCycleData> buildPointCycleInfo(long time) {
        Map<String, PushCycleData> map = new HashMap<>();
        for (Map.Entry<String, PowerfulCycleTimeCounter> entry : PowerfulCounterService.getInstance().getPointCounterMap().entrySet()) {
            String point = entry.getKey();
            PowerfulCycleTimeCounter counter = entry.getValue();
            PushCycleData pushCycleData = PushCycleData.builder()
                    .concurrentNum(counter.getLastCycleConcurrentValue(time))
                    .exceptionNum(counter.getLastExceptionCycleValue(time))
                    .downgrateNum(counter.getLastDowngrateCycleValue(time))
                    .timeoutNum(counter.getLastTimeoutCycleValue(time))
                    .visitNum(counter.getLastSecondVisitBucketValue(time
                    ))
                    .point(point).build();
            map.put(point, pushCycleData);
        }
        return map;
    }


}
