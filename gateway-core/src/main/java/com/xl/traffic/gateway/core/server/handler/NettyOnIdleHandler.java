package com.xl.traffic.gateway.core.server.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.enums.MsgType;
import com.xl.traffic.gateway.core.utils.SnowflakeIdWorker;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class NettyOnIdleHandler extends ChannelDuplexHandler {


    private RpcMsg heartCmd;


    /**
     * 构造心跳消息
     */
    public NettyOnIdleHandler() {
        heartCmd = new RpcMsg();
        heartCmd.setReqId(SnowflakeIdWorker.getInstance().nextId().intValue());
        heartCmd.setCmd((byte) MsgType.HEAT_CMD.getType());
    }

    /**
     * 心跳检测
     *
     * @param ctx
     * @param evt
     * @return: void
     * @author: xl
     * @date: 2021/6/24
     **/
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //心跳配置
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                ctx.writeAndFlush(heartCmd);
            } else if (event.state() == IdleState.READER_IDLE) {
                ctx.writeAndFlush(heartCmd);
            } else if (event.state() == IdleState.ALL_IDLE) {
                ctx.close();
            }
        }
    }
}
