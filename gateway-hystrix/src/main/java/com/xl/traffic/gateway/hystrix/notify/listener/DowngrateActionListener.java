package com.xl.traffic.gateway.hystrix.notify.listener;

import com.xl.traffic.gateway.hystrix.enums.DowngradeActionType;

import java.util.Date;

/**
 * 降级触发器
 *
 * @author: xl
 * @date: 2021/6/28
 **/
public interface DowngrateActionListener {


    /**
     * 降级触发通知
     *
     * @param point               降级点名称
     * @param downgradeActionType 降级触发类型
     * @param time                降级时间
     * @return: void
     * @author: xl
     * @date: 2021/6/28
     **/
    void downgrateAction(String point, DowngradeActionType downgradeActionType, Date time);

}
