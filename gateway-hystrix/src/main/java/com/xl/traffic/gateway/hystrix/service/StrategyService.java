package com.xl.traffic.gateway.hystrix.service;

import com.xl.traffic.gateway.core.gson.GSONUtil;
import com.xl.traffic.gateway.hystrix.model.Strategy;
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


    /**
     * 保存所有降级点和降级策略的映射关系
     */
    @Getter
    private ConcurrentHashMap<String, Strategy> strategyMap = new ConcurrentHashMap<>();

    /**
     * 通过降级点获取策略
     *
     * @param point 降级点名称
     * @return: com.xl.traffic.gateway.hystrix.model.Strategy
     * @author: xl
     * @date: 2021/6/25
     **/
    public Strategy getStrategy(String point) {
        return strategyMap.get(point);
    }

    /**
     * 更新某个降级策略信息
     *
     * @param point    降级点名称
     * @param strategy 降级策略
     * @return: true-更新成功 false-更新失败
     * @author: xl
     * @date: 2021/6/25
     **/
    public boolean updateStrategyByPoint(String point, Strategy strategy) {

        if (!StringUtils.isEmpty(point)) {
            Strategy oldStrategy = strategyMap.get(point);
            strategyMap.put(point, strategy);
            log.info("StrategyService 单个策略更新，旧：" + oldStrategy + "，新：" + strategy);
            /**重设单个降级延长*/
            DowngrateDelayService.getInstance().addOrUpdatePointDelayByPoint(point, strategy);
            /**更新单个的并发阈值*/
            PowerfulCounterService.getInstance().updateConcurrentByPoint(point, strategy.getConcurrentThreshold());
            return true;
        }
        return false;
    }


    public boolean updateAllStrategy(ConcurrentHashMap<String, Strategy> strategyMap) {
        if (null == strategyMap) {
            return false;
        }
        log.info("StrategyService 更新全部策略,旧：" + GSONUtil.toJson(this.strategyMap) + "新：" + GSONUtil.toJson(strategyMap));
        this.strategyMap = strategyMap;
        /**重设降级延长*/
        DowngrateDelayService.getInstance().addOrUpdatePointDelay(strategyMap);
        /**更新所有的并发阈值*/
        PowerfulCounterService.getInstance().updateConcurrent();
        return true;
    }


}
