package com.chatgpt.server.server.handler;

import com.chatgpt.server.message.ImMessage;
import com.chatgpt.server.queue.QueueManager;
import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.gson.GSONUtil;
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
@Component
@Slf4j
public class ChatGptAskHandler implements ChatGptServerHandlerService{

    @Override
    public void execute(RpcMsg rpcMsg, Channel channel) {
        ImMessage imMessage = GSONUtil.fromJson(new String(rpcMsg.getBody()), ImMessage.class);
        QueueManager.pushInMessage(imMessage);
    }
}
