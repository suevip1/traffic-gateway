package com.xl.traffic.gateway.rpc.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.core.enums.MsgCMDType;
import com.xl.traffic.gateway.core.mq.MQProvider;
import com.xl.traffic.gateway.core.utils.AttributeKeys;
import com.xl.traffic.gateway.core.utils.SnowflakeIdWorker;
import com.xl.traffic.gateway.rpc.connect.NodePoolCache;
import com.xl.traffic.gateway.rpc.process.RpcMsgProcess;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.time.Duration;
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
            /**处理业务*/
            RpcMsg result = RpcMsgProcess.getInstance().onMessageProcess(rpcMsg);
            /**返回消息*/
            channelHandlerContext.writeAndFlush(result);
        });
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.error("channelInactive(" + ctx + ")");
        String rpcServer = ctx.channel().attr(AttributeKeys.RPC_SERVER).get();
        Integer rpcPort = ctx.channel().attr(AttributeKeys.RPC_PORT).get();
        Integer rpcIndex = ctx.channel().attr(AttributeKeys.RPC_INDEX).get();

        String localAddress = ctx.channel().localAddress().toString();
        String remoteAddress = ctx.channel().remoteAddress().toString();
        logger.error("连接非活动!!!! rpcServer={}, rpcPort={}, channel={}, localAddress={}", rpcServer, rpcPort, ctx.channel(), localAddress);

        closeChannel(ctx);
        //todo 恢复重连
        //解决IP为0.0.0.0/0.0.0.0:33703的问题
//        if(localAddress.startsWith("0.0.0.0") || remoteAddress.startsWith("0.0.0.0")){
//            //停止
//            logger.error("localAddress={} 为无效地址, 停止重连!", localAddress);
//        }else{
//
//
//        }
        logger.info("开始执行重连业务...");
        //重连连接
        ServerNodeInfo nodeInfo = new ServerNodeInfo();
        nodeInfo.setRpcServerIndex(rpcIndex);
        nodeInfo.setIp(rpcServer);
        nodeInfo.setPort(rpcPort);
        MQProvider.getRetryConnectQueue().push(nodeInfo, Duration.ofMillis(500));
    }

    private void closeChannel(ChannelHandlerContext ctx) throws Exception {
        //清除map中连接信息
        String rpcPoolIndex = ctx.channel().attr(AttributeKeys.RPC_POOL_KEY).get();
        String rpcServer = ctx.channel().attr(AttributeKeys.RPC_SERVER).get();
        String rpcGroup = ctx.channel().attr(AttributeKeys.RPC_GROUP).get();
        NodePoolCache.removeActionRpcSrv(rpcGroup, rpcServer, rpcPoolIndex);
        logger.info("清除rpcPoolIndex={}", rpcPoolIndex);
        ctx.channel().close().sync();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("exceptionCaught(" + ctx + ")", cause);
        if (cause instanceof ConnectException) {
            String rpcServer = ctx.channel().attr(AttributeKeys.RPC_SERVER).get();
            Integer rpcPort = ctx.channel().attr(AttributeKeys.RPC_PORT).get();
            Integer rpcIndex = ctx.channel().attr(AttributeKeys.RPC_INDEX).get();
            String rpcPoolKey = ctx.channel().attr(AttributeKeys.RPC_POOL_KEY).get();
            String rpcGroup = ctx.channel().attr(AttributeKeys.RPC_GROUP).get();
            Thread.sleep(1000 * 15);
            logger.error("try connect tx-manager:{} ", ctx.channel().remoteAddress());
            NodePoolCache.removeActionRpcSrv(rpcGroup, rpcServer, rpcPoolKey);

            //重连连接
            ServerNodeInfo nodeInfo = new ServerNodeInfo();
            nodeInfo.setRpcServerIndex(rpcIndex);
            nodeInfo.setIp(rpcServer);
            nodeInfo.setPort(rpcPort);
            MQProvider.getRetryConnectQueue().push(nodeInfo, Duration.ofMillis(500));

        }
        /**发送心跳探活*/
        ctx.channel().writeAndFlush(heartCmd);
    }


}
