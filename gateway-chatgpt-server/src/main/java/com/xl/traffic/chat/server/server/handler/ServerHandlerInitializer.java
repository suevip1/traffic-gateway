package com.xl.traffic.chat.server.server.handler;

import com.xl.traffic.gateway.core.enums.MsgCMDType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * handler 初始化
 *
 * @author: xl
 * @date: 2021/7/5
 **/
@Component
public class ServerHandlerInitializer {

    private static class InstanceHolder {
        private static ServerHandlerInitializer instance = new ServerHandlerInitializer();
    }

    public static ServerHandlerInitializer getInstance() {
        return InstanceHolder.instance;
    }

    private ConcurrentMap<Integer, ChatGptServerHandlerService> handlerMap = new ConcurrentHashMap<Integer, ChatGptServerHandlerService>();

    @Autowired
    ChatGptAskHandler chatGptAskHandler;

    @Autowired
    ChatGptPullHistoryMsgHandler chatGptPullHistoryMsgHandler;

    /**
     * 获取Handler
     *
     * @param cmd
     * @return
     */
    public ChatGptServerHandlerService getHandler(int cmd) {
        return handlerMap.get(cmd);
    }

    @PostConstruct
    private void init() {
        handlerMap.put((int) MsgCMDType.CHAT_GPT_RECEIVE_ASK_CMD.getType(), chatGptAskHandler);
        handlerMap.put((int) MsgCMDType.PULL_CHAT_GPT_HISTORY_MSG_CMD.getType(), chatGptPullHistoryMsgHandler);
    }


}
