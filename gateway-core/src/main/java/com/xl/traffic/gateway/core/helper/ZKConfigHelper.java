package com.xl.traffic.gateway.core.helper;

import com.github.zkclient.IZkDataListener;
import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.register.zookeeper.ZkHelp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ZKConfigHelper {


    private final static Logger log = LoggerFactory.getLogger(ZKConfigHelper.class);

    private final static String configZkPath = GatewayConstants.CONFIG;

    private List<ServerNodeInfo> serverNodeInfos = null;

    private ZkHelp zkHelp = ZkHelp.getInstance();


    private static IZkDataListener listenerGlobal = null;


    public List<ServerNodeInfo> getServerNodeInfos() {
        return serverNodeInfos;
    }
}
