package com.xl.traffic.gateway.core.parser;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * http协议转换，与后端业务服务通信
 */
public class NettyHttpRequestParser {


    /**
     * http协议转换
     *
     * @param request
     * @return: java.util.Map<java.lang.String, java.lang.String>
     * @author: xl
     * @date: 2021/6/24
     **/
    public static Map<String, String> Parse(FullHttpRequest request) {
        Map<String, String> paramMap = new HashMap<>();
        HttpMethod method = request.method();
        if (method == HttpMethod.GET) {
            QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
            for (Map.Entry<String, List<String>> attr : decoder.parameters().entrySet()) {
                for (String attrVal : attr.getValue()) {
                    paramMap.put(attr.getKey(), attrVal);
                }
            }
        } else if (method == HttpMethod.POST) {
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
            List<InterfaceHttpData> paramList = decoder.getBodyHttpDatas();
            for (InterfaceHttpData param : paramList) {
                Attribute data = (Attribute) param;
                try {
                    paramMap.put(data.getName(), data.getValue());
                } catch (IOException ignore) {
                }
            }
            decoder.destroy();
        }

        return paramMap;
    }
}
