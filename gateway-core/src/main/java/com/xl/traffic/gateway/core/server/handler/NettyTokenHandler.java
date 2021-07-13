package com.xl.traffic.gateway.core.server.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.context.NettyContext;
import com.xl.traffic.gateway.core.enums.MsgCMDType;
import com.xl.traffic.gateway.core.enums.NettyType;
import com.xl.traffic.gateway.core.enums.SerializeType;
import com.xl.traffic.gateway.core.serialize.ISerialize;
import com.xl.traffic.gateway.core.serialize.SerializeFactory;
import com.xl.traffic.gateway.core.token.Token;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class NettyTokenHandler extends SimpleChannelInboundHandler<RpcMsg> {

    ISerialize serialize = SerializeFactory.getInstance().getISerialize(SerializeType.protobuf);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMsg cmd) throws Exception {

        /**排除登录指令*/
        if (cmd.getCmd() == MsgCMDType.LOGIN_CMD.getType()) {
            return;
        }
        /**token校验*/
        String token = new String(cmd.getToken());
        if (Token.getValue(token) == null) {
            log.info("token 已过期！reqId:{}", cmd.getReqId());
            //发送消息，token失效，断开连接 cmd
            cmd.setCmd(MsgCMDType.DISCONNECT.getType());
            cmd.setBody(serialize.serialize("token过期断开连接！"));
            ctx.channel().writeAndFlush(cmd).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    //断开连接
                    ctx.channel().close();
                }
            });
            return;
        }
        // 通知执行下一个InboundHandler
        ctx.fireChannelRead(cmd);
    }
}
