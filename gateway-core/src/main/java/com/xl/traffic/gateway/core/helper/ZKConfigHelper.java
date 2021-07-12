package com.xl.traffic.gateway.core.helper;

import com.alibaba.fastjson.JSONObject;
import com.github.zkclient.IZkDataListener;
import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.core.config.GateWayConfig;
import com.xl.traffic.gateway.core.config.GatewayCommonConfig;
import com.xl.traffic.gateway.core.gson.GSONUtil;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.register.zookeeper.ZkHelp;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ZKConfigHelper {



    private static class InstanceHolder {
        public static final ZKConfigHelper instance = new ZKConfigHelper();
    }

    public static ZKConfigHelper getInstance() {
        return ZKConfigHelper.InstanceHolder.instance;
    }


    private final static Logger log = LoggerFactory.getLogger(ZKConfigHelper.class);
    private final static String configZkPath = GatewayConstants.CONFIG;
    private List<ServerNodeInfo> serverNodeInfos = null;
    private ZkHelp zkHelp = ZkHelp.getInstance();
    private static IZkDataListener listenerGlobal = null;

    public List<ServerNodeInfo> getServerNodeInfos() {
        return serverNodeInfos;
    }

    @Getter
    @Setter
    private GatewayCommonConfig gatewayCommonConfig = null;

    public ZKConfigHelper() {

        listenerGlobal = new IZkDataListener() {

            @Override
            public void handleDataChange(String dataPath, byte[] data) throws Exception {
                log.info("!!! configZkPath node data has been changed !!!" + dataPath);
                String rtdata = null;
                if (data != null && data.length > 0) {
                    rtdata = new String(data, "UTF-8");
                    JSONObject json = JSONObject.parseObject(rtdata);
                    // read gatewayCommonConfig
                    String gatewayCommonNode = json.getString("gatewayCommonconfig");
                    gatewayCommonConfig = GSONUtil.fromJson(gatewayCommonNode, GatewayCommonConfig.class);
                }
                log.info("!!! configZkPath node data has been changed ok !!!" + rtdata + ", gatewayCommonconfig=[" + gatewayCommonConfig + "]");
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                log.info("!!! configZkPath node dataPath has been delete !!!" + dataPath + ", gatewayCommonconfig=[" + gatewayCommonConfig + "]");
            }
        };
        // 添加节点监控
        zkHelp.subscribeDataChanges(configZkPath, listenerGlobal);
        log.info("===================init ZkConfigTobHelper ok================");

    }
}
