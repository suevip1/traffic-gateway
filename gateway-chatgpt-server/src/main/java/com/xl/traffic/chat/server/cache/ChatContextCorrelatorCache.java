package com.xl.traffic.chat.server.cache;

import com.xl.traffic.chat.server.chatgpt.request.OpenAiCompletionRequest;
import com.xl.traffic.chat.server.config.ChatGptConfig;
import com.xl.traffic.chat.server.message.ImMessage;
import com.xl.traffic.chat.server.utils.CircularListCodecUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * ChatContextCorrelator
 *
 * @author chenx
 */
@Slf4j
public class ChatContextCorrelatorCache {
    private static class InstanceHolder {
        public static final ChatContextCorrelatorCache instance = new ChatContextCorrelatorCache();
    }

    public static ChatContextCorrelatorCache getInstance() {
        return ChatContextCorrelatorCache.InstanceHolder.instance;
    }

    private Cache<String, byte[]> contextCache =  Caffeine.newBuilder()
            .initialCapacity(1024)
            .maximumSize(10000)
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .build();;
    private ChatGptConfig config;
    public static final String SERVICE_AI_MODEL_TXT = "text-davinci-003";
    public static final int SERVICE_CHOICE_COUNT = 1;

    /**
     * getOpenAiCompletionRequest
     *
     * @param message
     * @return
     */
    public OpenAiCompletionRequest getOpenAiCompletionRequest(ImMessage message) throws IOException {
        String key = this.getKey(message);
        String value = message.getContent();
        ConcurrentCircularList<String> chatContextList = this.setContext(key, value);
        return this.buildOpenAiCompletionRequest(chatContextList);
    }

    /**
     * @param message
     * @return
     */
    public String getKey(ImMessage message) {
        if (Objects.isNull(message)) {
            throw new RuntimeException("The input ImMessage is null!");
        }
        return message.getFromUserId();
    }

    /**
     * setContext
     *
     * @param key
     * @param context
     * @return
     */
    public ConcurrentCircularList<String> setContext(String key, String context) throws IOException {
        byte[] data = this.contextCache.getIfPresent(key);
        ConcurrentCircularList<String> circularList = ArrayUtils.isEmpty(data)
                ? new ConcurrentCircularList<>(this.config.getContextCacheRingCapacity())
                : CircularListCodecUtils.decodeStringList(data);
        circularList.add(context);
        this.contextCache.put(key, CircularListCodecUtils.encodeStringList(circularList, this.config.getContextCacheRingCapacity()));

        return circularList;
    }

    /**
     * buildOpenAiCompletionRequest
     *
     * @param chatContextList
     * @return
     */
    private OpenAiCompletionRequest buildOpenAiCompletionRequest(ConcurrentCircularList<String> chatContextList) {
        if (chatContextList == null || chatContextList.isEmpty()) {
            throw new RuntimeException("chatContextList is null or empty!");
        }
        StringBuilder sb = new StringBuilder();
        for (String content : chatContextList) {
            sb.append(content + " ");
        }
        return OpenAiCompletionRequest.builder()
                .prompt(sb.toString())
                .model(SERVICE_AI_MODEL_TXT)
                .temperature(this.config.getOpenAiTemperature())
                .maxTokens(this.config.getOpenAiMaxTokens())
                .n(SERVICE_CHOICE_COUNT)
                .build();
    }
}
