package com.xl.traffic.gateway.core.loadbalance.strategy;


import com.xl.traffic.gateway.core.enums.LoadBalanceType;
import com.xl.traffic.gateway.core.loadbalance.RpcLoadBalance;

import java.util.List;

public class RpcLoadBalanceStrategy {


    private static class InstanceHolder {
        public static final RpcLoadBalanceStrategy instance = new RpcLoadBalanceStrategy();
    }

    public static RpcLoadBalanceStrategy getInstance() {
        return RpcLoadBalanceStrategy.InstanceHolder.instance;
    }

    private List<RpcLoadBalance> rpcLoadBalanceList;

    public RpcLoadBalanceStrategy(List<RpcLoadBalance> rpcLoadBalanceList) {
        this.rpcLoadBalanceList = rpcLoadBalanceList;
    }

    public RpcLoadBalanceStrategy() {
    }

    public RpcLoadBalance getRpcLoadBalance(LoadBalanceType balanceType) {
        for (RpcLoadBalance commitor : rpcLoadBalanceList) {
            if (commitor.getType().equals(balanceType)) {
                return commitor;
            }
        }
        return null;
    }

}
