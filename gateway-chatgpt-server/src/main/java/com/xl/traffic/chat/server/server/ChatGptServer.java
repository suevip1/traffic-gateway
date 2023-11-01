package com.xl.traffic.chat.server.server;

import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.server.AbstractNettyServerInlization;
import com.xl.traffic.gateway.core.server.Server;
import com.xl.traffic.gateway.core.utils.GatewayPortConstants;

/**
 * @author xuliang
 * @version 1.0
 * @project traffic-gateway
 * @description
 * @date 2023/10/26 14:46:58
 */
public class ChatGptServer extends AbstractNettyServerInlization implements Server {
    public ChatGptServer() {
        super(AddressUtils.getInnetIp(), GatewayPortConstants.TCP_CHATGPT_PORT);
    }
    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
