package com.chatgpt.server.config;

import lombok.Data;

/**
 * @author xuliang
 * @version 1.0
 * @project traffic-gateway
 * @description
 * @date 2023/10/26 15:32:30
 */
@Data
public class ChatGptConfig {


    private Long receiveQueueNum;
    private Long sendQueueNum;
    private String openAiApiAuth;

    private String openAiAskApiUrl;

    private int openAiMaxTokens;
    private Double openAiTemperature;
    private int contextCacheRingCapacity;
    private int contextCacheInitialCapacity;

    private int contextCacheMaxCapacity;

    private int contextCacheExpireSeconds;


}
