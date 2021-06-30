package com.xl.traffic.gateway.hystrix.enums;

import lombok.Getter;
import lombok.Setter;

/**
 * 策略类型
 *
 * @author: xl
 * @date: 2021/6/24
 **/

public enum DowngradeActionType {


    VISIT(1,"访问量降级策略"),
    CONCURRENT(2, "并发降级策略"),
    EXCEPTION(3, "异常量降级策略"),
    EXCEPTION_RATE(4, "异常率降级策略"),
    TIMEOUT(5, "超时降级策略"),
    TOKEN_BUCKET(6, "令牌桶降级策略"),
    PRESSURE_TEST(7, "压测降级策略"),
    FUSE(8, "熔断策略"),
    ;

    @Getter
    @Setter
    private int type;
    @Getter
    @Setter
    private String desc;

    DowngradeActionType(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

}
