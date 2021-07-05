package com.xl.traffic.gateway.hystrix.service;

import com.xl.traffic.gateway.core.gson.GSONUtil;
import com.xl.traffic.gateway.hystrix.model.Strategy;
import com.xl.traffic.gateway.hystrix.utils.CacheKeyUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 降级策略服务管理
 *
 * @author: xl
 * @date: 2021/6/25
 **/
@Slf4j
public class StrategyService {


    private final static StrategyService strategyService = new StrategyService();


    public static StrategyService getInstance() {
        return strategyService;
    }

    private StrategyService() {
    }


    //todo 改变一下存储结构，key=appGroupName+appName+point
    /**
     * 保存所有降级点和降级策略的映射关系
     */
    @Getter
    private ConcurrentHashMap<String, Strategy> strategyMap = new ConcurrentHashMap<>();

    /**
     * 通过应用组+应用+降级点获取策略
     *
     * @param appGroupName 应用组
     * @param appName      应用
     * @param point        降级点
     * @return: com.xl.traffic.gateway.hystrix.model.Strategy
     * @author: xl
     * @date: 2021/7/5
     **/
    public Strategy getStrategy(String appGroupName, String appName, String point) {
        return strategyMap.get(CacheKeyUtil.getAppGroupWithAppNameWithPoint(appGroupName, appName, point));
    }


    /**
     * 更新某个降级策略信息
     *
     * @param appGroupName 应用组
     * @param appName      应用
     * @param point        降级点
     * @return: com.xl.traffic.gateway.hystrix.model.Strategy
     * @author: xl
     * @date: 2021/7/5
     **/
    public boolean updateStrategyByPoint(String appGroupName, String appName, String point, Strategy strategy) {

        String key = CacheKeyUtil.getAppGroupWithAppNameWithPoint(appGroupName, appName, point);
        if (!StringUtils.isEmpty(key)) {
            Strategy oldStrategy = strategyMap.get(key);
            strategyMap.put(key, strategy);
            log.info("StrategyService 单个策略更新，旧：" + oldStrategy + "，新：" + strategy);
            /**重设单个降级延长*/
            DowngrateDelayService.getInstance().addOrUpdatePointDelayByPoint(appGroupName, appName, key, strategy);
            /**更新单个的并发阈值*/
            PowerfulCounterService.getInstance().updateConcurrentByPoint(appGroupName, appName, key, strategy.getConcurrentThreshold());
            return true;
        }
        return false;
    }


    public boolean updateAllStrategy(String appGroupName, String appName, ConcurrentHashMap<String, Strategy> strategyMap) {
        if (null == strategyMap) {
            return false;
        }
        log.info("StrategyService 更新全部策略,旧：" + GSONUtil.toJson(this.strategyMap) + "新：" + GSONUtil.toJson(strategyMap));
        this.strategyMap = strategyMap;
        /**重设降级延长*/
        DowngrateDelayService.getInstance().addOrUpdatePointDelay(appGroupName, appName, strategyMap);
        /**更新所有的并发阈值*/
        PowerfulCounterService.getInstance().updateConcurrent(appGroupName, appName, strategyMap);
        return true;
    }


}
