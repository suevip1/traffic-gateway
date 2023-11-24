package com.xl.traffic.gateway.rpc.mq.send;

import com.lmax.disruptor.WorkHandler;
import com.xl.traffic.chat.server.message.ImMessage;
import com.xl.traffic.chat.server.queue.MessageEvent;


/**
 * @author xuliang
 * @version 1.0
 * @project traffic-gateway
 * @description
 * @date 2023/10/26 15:55:04
 */
public class ChatGptSendConsumer implements WorkHandler<MessageEvent<ImMessage>> {

    @Override
    public void onEvent(MessageEvent<ImMessage> imMessageMessageEvent) throws Exception {

        // TODO: 2023/10/26 调用router发送IM消息

    }
}
