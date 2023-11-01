package com.xl.traffic.chat.server.ratelimit;

import com.xl.traffic.chat.server.enums.MsgType;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author xuliang
 * @version 1.0
 * @project traffic-gateway
 * @description
 * @date 2023/10/26 17:48:54
 */
public class RateLimitManager {
    private static class InstanceHolder {
        public static final RateLimitManager instance = new RateLimitManager();
    }

    public static RateLimitManager getInstance() {
        return RateLimitManager.InstanceHolder.instance;
    }

    /**
     * @param msgType
     * @return boolean
     * @description 消息速率控制
     * @author xuliang
     * @date 2023/10/26 18:02:06
     */
    public boolean tryAcquire(MsgType msgType) {
        Long msgRate = null;
        switch (msgType) {
            case WHITE:
                msgRate = 1L;
                break;
            case HIGN:
                msgRate = highLevelMsg();
                break;
            case MIDDLE:
                msgRate = middleLevelMsg();
                break;
            case LOW:
                msgRate = normalLevelMsg();
                break;
            default:
                break;
        }

        if (null != msgRate && msgRate > 0) {
            return true;
        }
        return false;

    }

    public Long highLevelMsg() {
        Long sumConcurrency = 100l;
        Long highLevelMsgWeight = 70l;
        /**高优消息 每秒并发速率*/
        BigDecimal rate = new BigDecimal(highLevelMsgWeight).divide(BigDecimal.valueOf(100)).setScale(2, RoundingMode.DOWN).multiply(BigDecimal.valueOf(sumConcurrency));
        Long highLevelMsgRate = rate.longValue();
        /**redis漏桶*/

        return highLevelMsgRate;
    }


    public Long middleLevelMsg() {
        Long sumConcurrency = 100l;
        Long highLevelMsgWeight = 20l;
        /**高优消息 每秒并发速率*/
        BigDecimal rate = new BigDecimal(highLevelMsgWeight).divide(BigDecimal.valueOf(100)).setScale(2, RoundingMode.DOWN).multiply(BigDecimal.valueOf(sumConcurrency));
        Long highLevelMsgRate = rate.longValue();
        /**redis漏桶*/

        return highLevelMsgRate;
    }


    public Long normalLevelMsg() {
        Long sumConcurrency = 100l;
        Long highLevelMsgWeight = 10l;
        /**高优消息 每秒并发速率*/
        BigDecimal rate = new BigDecimal(highLevelMsgWeight).divide(BigDecimal.valueOf(100)).setScale(2, RoundingMode.DOWN).multiply(BigDecimal.valueOf(sumConcurrency));
        Long highLevelMsgRate = rate.longValue();
        /**redis漏桶*/

        return highLevelMsgRate;
    }


}
