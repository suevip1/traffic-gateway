package com.chatgpt.server.server.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author xuliang
 * @version 1.0
 * @project traffic-gateway
 * @description
 * @date 2023/10/26 14:53:50
 */
@Slf4j
@Component
public class ChatGptPullHistoryMsgHandler implements ChatGptServerHandlerService{


    @Override
    public void execute(RpcMsg rpcMsg, Channel channel) {

    }
}
