package com.xl.traffic.gateway.core.utils;

import com.xl.traffic.gateway.core.gson.GSONUtil;
import lombok.Getter;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * OkHttpUtil
 *
 * @author: layne.xu
 * @date: 2022/07/13 4:00 PM
 **/

public class OkHttpUtil {

    public final static Logger logger = LoggerFactory.getLogger(OkHttpUtil.class);
    /**
     * 最大连接时间 5分钟
     */
    public final static int CONNECTION_TIMEOUT = 5 * 60;
    public final static int READ_TIMEOUT = 5 * 60;
    /**
     * JSON格式
     */
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    /**
     * OkHTTP线程池最大空闲线程数
     */
    public final static int MAX_IDLE_CONNECTIONS = 100;
    /**
     * OkHTTP线程池空闲线程存活时间
     */
    public final static long KEEP_ALIVE_DURATION = 30L;


    /**
     * client
     * 配置重试
     */
    @Getter
    private final static OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(MAX_IDLE_CONNECTIONS, KEEP_ALIVE_DURATION, TimeUnit.MINUTES))
            .build();


    /**
     * get请求，无需转换对象
     *
     * @param url     链接
     * @param headers 请求头
     * @return 响应信息
     */
    public static Response get(String url, Map<String, String> headers) {
        try {
            Request.Builder builder = new Request.Builder();
            buildHeader(builder, headers);
            Request request = builder.url(url).build();
            Response response = HTTP_CLIENT.newCall(request).execute();
            logger.info("执行get请求, url: {} 成功，返回数据: {}", url, response);
            return response;
        } catch (Exception e) {
            logger.error("执行get请求，url: {} 失败!", url, e);
        }
        return null;
    }


    /**
     * Form表单提交
     *
     * @param url    地址
     * @param params form参数
     * @return
     */
    public static Response post(String url, Map<String, String> params, Map<String, String> headers) {
        try {

            FormBody.Builder builder = new FormBody.Builder();
            if (!CollectionUtils.isEmpty(params)) {
                params.forEach(builder::add);
            }
            FormBody body = builder.build();
            Request request = new Request.Builder().url(url).post(body).build();
            Response response = HTTP_CLIENT.newCall(request).execute();
            logger.info("执行post请求,url: {} ,参数: {} 成功，返回结果: {}", url, params, response);

            return response;
        } catch (Exception e) {
            logger.error("", e);
        }
        return null;
    }


    /**
     * 简单post请求
     *
     * @param url     请求url
     * @param headers 请求头
     * @param json    请求参数
     * @return
     */
    public static Response post(String url, Map<String, String> headers, String json) {
        try {
            RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, json);
            Request.Builder builder = new Request.Builder();
            buildHeader(builder, headers);
            Request request = builder.url(url).post(body).build();
            Response response = HTTP_CLIENT.newCall(request).execute();
            logger.info("执行post请求,url: {},\n  header: {} ,\n 参数: {} 成功，返回结果: {}", url, headers, json, response);
            return response;
        } catch (Exception e) {
            logger.error("执行post请求，url: {},参数: {} 失败!", url, json, e);
        }
        return null;
    }


    /**
     * 设置请求头
     *
     * @param builder .
     * @param headers 请求头
     */
    private static void buildHeader(Request.Builder builder, Map<String, String> headers) {
        if (Objects.nonNull(headers) && headers.size() > 0) {
            headers.forEach((k, v) -> {
                if (Objects.nonNull(k) && Objects.nonNull(v)) {
                    builder.addHeader(k, v);
                }
            });
        }
    }


    /**
     * 支持嵌套泛型的post请求。
     * <pre>
     *   Type type = new TypeToken<Results<User>>() {}.getType();
     * <pre/>
     *
     * @param url     链接
     * @param headers 请求头
     * @param json    请求json
     * @param type    嵌套泛型
     * @return 响应对象, 可进行强转。
     */
    public static <T> T post(String url, Map<String, String> headers, String json, Type type) {
        String result = null;
        try {
            result = post(url, headers, json).body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (Objects.nonNull(result) && Objects.nonNull(type)) {
            return GSONUtil.fromJson(result, type);
        }
        return null;
    }


    public static Map<String,String> buildHeaderMap(String userId,String email,String groupChatId){
        Map<String, String> headers = new HashMap<>();
        headers.put("SenderUserId", userId);
        headers.put("SenderEmail", email);
        headers.put("groupChatId", groupChatId);
        return headers;
    }

    public static Map<String,String> buildAuthorizationMap(String token){
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer "+token);
        return headers;
    }
}
