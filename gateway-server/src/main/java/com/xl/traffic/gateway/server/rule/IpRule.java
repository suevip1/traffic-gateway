package com.xl.traffic.gateway.server.rule;

import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleType;

import java.net.InetSocketAddress;

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
