package com.xl.traffic.gateway.server.http.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.enums.MsgAppNameType;
import com.xl.traffic.gateway.core.enums.MsgCMDType;
import com.xl.traffic.gateway.core.enums.MsgGroupType;
import com.xl.traffic.gateway.core.enums.SerializeType;
import com.xl.traffic.gateway.core.gson.GSONUtil;
import com.xl.traffic.gateway.core.log.LogHelper;
import com.xl.traffic.gateway.core.parser.NettyHttpRequestParser;
import com.xl.traffic.gateway.core.serialize.ISerialize;
import com.xl.traffic.gateway.core.serialize.SerializeFactory;
import com.xl.traffic.gateway.router.RpcMsgRouter;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@ChannelHandler.Sharable
public class NettyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static Logger logger = LoggerFactory.getLogger(NettyHttpServerHandler.class);

    ISerialize iSerialize = SerializeFactory.getInstance().getISerialize(SerializeType.protobuf);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (!request.decoderResult().isSuccess()) {
            if (LogHelper.isDebugEnabled()) {
                logger.debug("NettyHttpServerHandler channelRead0 receive a bad http request, return");
            }
            ctx.close();
            return;
        }
        if (request.method() != HttpMethod.POST && request.method() != HttpMethod.GET) {
            if (LogHelper.isDebugEnabled()) {
                logger.debug("NettyHttpServerHandler channelRead0 unsupported http method: {}", request.method());
            }
            sendResponse(ctx, HttpResponseStatus.BAD_REQUEST, "{\"code\":1,\"msg\":\"unsupported http method\"}");
            return;
        }
        logger.debug("NettyHttpServerHandler channelRead0 new request: {}", request);
        if (request.uri().startsWith("/gateway/route/")) {
            Map<String, String> paramMap = NettyHttpRequestParser.Parse(request);
            //TODO 目前只走同步的，后续性能有问题时，区分同步请求还是异步请求
            String cmd = paramMap.get("cmd");
            String group = paramMap.get("group");
            String appName = paramMap.get("appName");
            String reqId = paramMap.get("reqId");
            String token = paramMap.get("token");
            String body = paramMap.get("body");
            RpcMsg rpcMsg = new RpcMsg(MsgCMDType.valueOf(cmd).getType(),
                    MsgGroupType.valueOf(group).getType(),
                    MsgAppNameType.valueOf(appName).getType(),
                    Long.valueOf(reqId), Long.valueOf(token), iSerialize.serialize(body));
            RpcMsg rpcResultMsg = RpcMsgRouter.getInstance().sendSync(rpcMsg);
            String result = GSONUtil.toJson(new String(rpcResultMsg.getBody()));
            sendResponse(ctx, HttpResponseStatus.OK, result);
        } else if (request.uri().startsWith("/gateway/control/enableDebugLog?")) {
            Map<String, String> paramMap = NettyHttpRequestParser.Parse(request);
            String enable = MapUtils.getString(paramMap, "enable");
            if ("1".equals(enable)) {
                LogHelper.setDebugEnabled(true);
            } else {
                LogHelper.setDebugEnabled(false);
            }
            sendResponse(ctx, HttpResponseStatus.OK, "{\"code\":0,\"msg\":\"success\"}");
            ctx.close();
        }
    }

    private static void sendResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String body) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer(body + "\r\n", CharsetUtil.UTF_8));

        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        if (status == HttpResponseStatus.OK) {
            response.headers().set(CONNECTION, "keep-alive");
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        } else {
            response.headers().set(CONNECTION, "close");
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
