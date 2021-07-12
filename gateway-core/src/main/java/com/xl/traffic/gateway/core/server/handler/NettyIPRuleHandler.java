package com.xl.traffic.gateway.core.server.handler;

import com.xl.traffic.gateway.core.cache.CaffineCacheUtil;
import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleType;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;

/**
 * IP限制，可用作黑白名单校验
 *
 * @author: xl
 * @date: 2021/7/7
 **/
public class NettyIPRuleHandler implements IpFilterRule {

    @Override
    public boolean matches(InetSocketAddress inetSocketAddress) {
        String remoteIp = inetSocketAddress.getHostString();
        if (!StringUtils.isEmpty(CaffineCacheUtil.getBlackIpCacheMap().getIfPresent(remoteIp))) {
            //返回true则执行过滤器
            return true;
        }
        //返回false表示不执行过滤器
        return false;
    }

    @Override
    public IpFilterRuleType ruleType() {
        return IpFilterRuleType.ACCEPT;
    }
}
