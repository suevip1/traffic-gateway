package com.xl.traffic.chat.server.chatgpt;

import com.alibaba.fastjson.JSON;
import com.xl.traffic.chat.server.cache.ChatContextCorrelatorCache;
import com.xl.traffic.chat.server.chatgpt.response.OpenAiCompletionResponse;
import com.xl.traffic.chat.server.config.ChatGptConfig;
import com.xl.traffic.chat.server.message.ImMessage;
import com.xl.traffic.chat.server.enums.ChatbotResultCode;
import com.xl.traffic.gateway.core.gson.GSONUtil;
import com.xl.traffic.gateway.core.utils.OkHttpUtil;
import com.xl.traffic.gateway.core.utils.RetryHelper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Objects;

/**
 * @author xuliang
 * @version 1.0
 * @project traffic-gateway
 * @description
 * @date 2023/10/26 15:14:53
 */
@Slf4j
public class ChatGptClient {
    private static class InstanceHolder {
        public static final ChatGptClient instance = new ChatGptClient();
    }

    public static ChatGptClient getInstance() {
        return ChatGptClient.InstanceHolder.instance;
    }

    ChatGptConfig chatGptConfig;

    public String send(ImMessage imMessage){
        return RetryHelper.retryWithReturn(5l,1000l,()->{
            // TODO: 2023/10/26  发起chatgpt请求
            String callbackContent = "";
            try {
                Map<String, String> authorizationHeaders = OkHttpUtil.buildAuthorizationMap(chatGptConfig.getOpenAiApiAuth());
                String requestBody = JSON.toJSONString(ChatContextCorrelatorCache.getInstance().getOpenAiCompletionRequest(imMessage));
                Response response = OkHttpUtil.post(chatGptConfig.getOpenAiAskApiUrl(), authorizationHeaders, requestBody);
                if (!response.isSuccessful()) {
                    log.error("ChatGptClient.send() failed, httpStatusCode:" + response.code() + ", " + imMessage.toString());
                    callbackContent = getErrorResult(ChatbotResultCode.CHAT_GPT_API_HTTP_STATUS_CODE_ERROR.getCode());
                    return callbackContent;
                }
                OpenAiCompletionResponse responseBody = GSONUtil.fromJson(response.body().string(),OpenAiCompletionResponse.class);
                if (Objects.isNull(responseBody)) {
                    log.error("ChatGptClient.send() failed, responseBody is null!");
                    callbackContent = getErrorResult(ChatbotResultCode.CHAT_GPT_API_RESPONSE_BODY_IS_NULL.getCode());
                    return callbackContent;
                }
                if (CollectionUtils.isEmpty(responseBody.getChoices())) {
                    log.error("ChatGptClient.send() failed, response choices is empty!");
                    callbackContent = getErrorResult(ChatbotResultCode.CHAT_GPT_API_RESPONSE_CHOICES_IS_EMPTY.getCode());
                    return callbackContent;
                }
                callbackContent = responseBody.getChoices().get(0).getText();
                ChatContextCorrelatorCache.getInstance().setContext(ChatContextCorrelatorCache.getInstance().getKey(imMessage), callbackContent);
            } catch (Exception ex) {
                log.error("ChatGptClient.send() error!", ex);
            }
            return callbackContent;
        });
    }



    /**
     * getErrorResult
     *
     * @param code
     * @return
     */
    private static String getErrorResult(int code) {
        return String.format("System Internal Error (%s)!", code);
    }








}
