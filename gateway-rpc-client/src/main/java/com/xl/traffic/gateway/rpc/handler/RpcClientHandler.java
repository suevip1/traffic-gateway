package com.xl.traffic.gateway.rpc.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.enums.MsgCMDType;
import com.xl.traffic.gateway.core.utils.SnowflakeIdWorker;
import com.xl.traffic.gateway.rpc.callback.Callback;
import com.xl.traffic.gateway.rpc.callback.CallbackPool;
import com.xl.traffic.gateway.rpc.manager.RpcChannelManager;
import com.xl.traffic.gateway.rpc.process.RpcMsgProcess;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@ChannelHandler.Sharable
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcMsg> {


    private static final Logger logger = LoggerFactory.getLogger(RpcClientHandler.class);
    private RpcMsg heartCmd;

    private static ExecutorService executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    /**
     * 构造心跳消息
     */
    public RpcClientHandler() {
        heartCmd = new RpcMsg();
        heartCmd.setReqId(SnowflakeIdWorker.getInstance().nextId().intValue());
        heartCmd.setCmd((byte) MsgCMDType.HEAT_CMD.getType());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcMsg rpcMsg) throws Exception {
        executors.execute(() -> {
            Callback<RpcMsg> cb = (Callback<RpcMsg>)CallbackPool.remove(rpcMsg.getReqId());
            if (cb == null) {
                //找不到回调//可能超时被清理了
                logger.warn("Receive msg from server but no context found, requestId=" + rpcMsg.getReqId() + "," + channelHandlerContext);
                return;
            }
            cb.handleResult(rpcMsg);
        });
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.error("channelInactive(" + ctx + ")");
        RpcChannelManager.getInstance().disconnect(ctx.channel());
    }


    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("exceptionCaught(" + ctx + ")", cause);
        RpcChannelManager.getInstance().exceptionCaught(ctx.channel(), cause);
    }


}
