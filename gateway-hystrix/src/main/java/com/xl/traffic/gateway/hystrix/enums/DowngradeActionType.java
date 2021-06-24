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


    CONCURRENT(2, "并发降级策略"),
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
