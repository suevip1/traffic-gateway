package com.chatgpt.server.queue.send;

import com.chatgpt.server.message.ImMessage;
import com.chatgpt.server.queue.MessageEvent;
import com.lmax.disruptor.WorkHandler;


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
