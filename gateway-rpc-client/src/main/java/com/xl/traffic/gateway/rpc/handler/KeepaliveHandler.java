package com.xl.traffic.gateway.rpc.handler;


import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.enums.MsgCMDType;
import com.xl.traffic.gateway.core.utils.SnowflakeIdWorker;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xl
 * @date: 2020-12-18
 * @desc: 心跳handler
 */
@ChannelHandler.Sharable
@Slf4j
public class KeepaliveHandler extends ChannelInboundHandlerAdapter {

    private RpcMsg heartCmd;

    /**
     * 构造心跳消息
     */
    public KeepaliveHandler() {
        heartCmd = new RpcMsg();
        heartCmd.setReqId(SnowflakeIdWorker.getInstance().nextId().intValue());
        heartCmd.setCmd((byte) MsgCMDType.HEAT_CMD.getType());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //心跳配置
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                ctx.writeAndFlush(heartCmd);
            } else if (event.state() == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush(heartCmd);
            } else if (event.state() == IdleState.ALL_IDLE) {
                ctx.writeAndFlush(heartCmd);
            }
        }
    }
}