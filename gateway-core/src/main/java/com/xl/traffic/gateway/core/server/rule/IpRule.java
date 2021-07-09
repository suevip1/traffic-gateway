package com.xl.traffic.gateway.core.server.rule;

import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleType;

import java.net.InetSocketAddress;

/**
 * IP限制，可用作黑白名单校验
 *
 * @author: xl
 * @date: 2021/7/7
 **/
public class IpRule implements IpFilterRule {


    @Override
    public boolean matches(InetSocketAddress inetSocketAddress) {
        return false;
    }

    @Override
    public IpFilterRuleType ruleType() {
        return IpFilterRuleType.ACCEPT;
    }
}
