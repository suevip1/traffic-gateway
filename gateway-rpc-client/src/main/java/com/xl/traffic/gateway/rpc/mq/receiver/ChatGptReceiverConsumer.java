package com.xl.traffic.gateway.rpc.mq.receiver;

import com.lmax.disruptor.WorkHandler;
import com.xl.traffic.chat.server.chatgpt.ChatGptClient;
import com.xl.traffic.chat.server.message.ImMessage;
import com.xl.traffic.chat.server.queue.MessageEvent;
import com.xl.traffic.chat.server.queue.QueueManager;
import com.xl.traffic.chat.server.ratelimit.RateLimitManager;
import com.xl.traffic.gateway.core.utils.RetryHelper;


/**
 * @author xuliang
 * @version 1.0
 * @project traffic-gateway
 * @description
 * @date 2023/10/26 15:55:04
 */
public class ChatGptReceiverConsumer implements WorkHandler<MessageEvent<ImMessage>> {

    @Override
    public void onEvent(MessageEvent<ImMessage> imMessageMessageEvent) throws Exception {
        RetryHelper.retry(5l, 1000l, () -> {
            try {
                ImMessage imMessage = imMessageMessageEvent.getMsg();
                if (RateLimitManager.getInstance().tryAcquire(imMessage.getMsgType())) {
                    String answer = ChatGptClient.getInstance().send(imMessage);
                    imMessage.setContent(answer);
                    QueueManager.pushOutMessage(imMessage);
                } else {
                    throw new RuntimeException("ChatGpt消息速率限流，进行重试!");
                }
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });
    }
}
