package com.xl.traffic.gateway.core.server.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.cache.LocalCacheService;
import com.xl.traffic.gateway.core.enums.MsgCMDType;
import com.xl.traffic.gateway.core.utils.SnowflakeIdWorker;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class NettyOnIdleHandler extends ChannelDuplexHandler {


    private RpcMsg heartCmd;

    private LocalCacheService localCacheService;


    /**
     * 构造心跳消息
     */
    public NettyOnIdleHandler() {
        heartCmd = new RpcMsg();
        heartCmd.setReqId(SnowflakeIdWorker.getInstance().nextId().intValue());
        heartCmd.setCmd((byte) MsgCMDType.HEAT_CMD.getType());
    }

    /**
     * 构造心跳消息
     */
    public NettyOnIdleHandler(LocalCacheService localCacheService) {
        heartCmd = new RpcMsg();
        heartCmd.setReqId(SnowflakeIdWorker.getInstance().nextId().intValue());
        heartCmd.setCmd((byte) MsgCMDType.HEAT_CMD.getType());
        this.localCacheService = localCacheService;
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        closeChannel(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        closeChannel(ctx);
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
            } else if (event.state() == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush(heartCmd);
            } else if (event.state() == IdleState.ALL_IDLE) {
                closeChannel(ctx);
            }
        }
    }

    /**
     * 关闭channel
     *
     * @param ctx
     * @return: void
     * @author: xl
     * @date: 2021/7/29
     **/
    public void closeChannel(ChannelHandlerContext ctx) throws InterruptedException {
        if (localCacheService != null) {
            localCacheService.removeLocalCache(ctx);
        }
        ctx.close().sync();
    }

}
