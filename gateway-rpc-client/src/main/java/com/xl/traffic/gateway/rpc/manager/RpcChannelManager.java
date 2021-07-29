package com.xl.traffic.gateway.rpc.manager;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.core.enums.MsgCMDType;
import com.xl.traffic.gateway.core.mq.MQProvider;
import com.xl.traffic.gateway.core.utils.AttributeKeys;
import com.xl.traffic.gateway.core.utils.SnowflakeIdWorker;
import com.xl.traffic.gateway.rpc.connect.NodePoolCache;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.time.Duration;

/**
 * rpc连接channel管理
 *
 * @author: xl
 * @date: 2021/7/20
 **/
public class RpcChannelManager {
    private RpcMsg heartCmd;

    private static Logger logger = LoggerFactory.getLogger(RpcChannelManager.class);

    private static class InstanceHolder {
        public static final RpcChannelManager instance = new RpcChannelManager();
    }

    public static RpcChannelManager getInstance() {
        return RpcChannelManager.InstanceHolder.instance;
    }


    public RpcChannelManager() {
        heartCmd = new RpcMsg();
        heartCmd.setReqId(SnowflakeIdWorker.getInstance().nextId().intValue());
        heartCmd.setCmd(MsgCMDType.HEAT_CMD.getType());
    }

    /**
     * 连接断开
     *
     * @param channel
     * @return: void
     * @author: xl
     * @date: 2021/7/20
     **/
    public void disconnect(Channel channel) throws Exception {
        String rpcServer = channel.attr(AttributeKeys.RPC_SERVER).get();
        Integer rpcPort = channel.attr(AttributeKeys.RPC_PORT).get();
        Integer rpcIndex = channel.attr(AttributeKeys.RPC_INDEX).get();

        String localAddress = channel.localAddress().toString();
        String remoteAddress = channel.remoteAddress().toString();
        logger.error("连接非活动!!!! rpcServer={}, rpcPort={}, localAddress={}", rpcServer, rpcPort, localAddress);
        closeChannel(channel);
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


    /**
     * 连接异常
     *
     * @param channel
     * @return: void
     * @author: xl
     * @date: 2021/7/20
     **/
    public void exceptionCaught(Channel channel, Throwable cause) throws Exception {
        if (cause instanceof ConnectException) {
            String rpcServer = channel.attr(AttributeKeys.RPC_SERVER).get();
            Integer rpcPort = channel.attr(AttributeKeys.RPC_PORT).get();
            Integer rpcIndex = channel.attr(AttributeKeys.RPC_INDEX).get();
            String rpcPoolKey = channel.attr(AttributeKeys.RPC_POOL_KEY).get();
            String rpcGroup = channel.attr(AttributeKeys.RPC_GROUP).get();
            Thread.sleep(1000 * 15);
            logger.error("try connect tx-manager:{} ", channel.remoteAddress());
            NodePoolCache.removeActionRpcSrv(rpcGroup, rpcServer, rpcPoolKey);

            //重连连接
            ServerNodeInfo nodeInfo = new ServerNodeInfo();
            nodeInfo.setRpcServerIndex(rpcIndex);
            nodeInfo.setIp(rpcServer);
            nodeInfo.setPort(rpcPort);
            MQProvider.getRetryConnectQueue().push(nodeInfo, Duration.ofMillis(500));

        }
        /**发送心跳探活*/
        channel.writeAndFlush(heartCmd);
    }


    /**
     * 关闭channel
     *
     * @param channel
     * @return: void
     * @author: xl
     * @date: 2021/7/20
     **/
    private void closeChannel(Channel channel) throws Exception {
        //清除map中连接信息
        String rpcPoolIndex = channel.attr(AttributeKeys.RPC_POOL_KEY).get();
        String rpcServer = channel.attr(AttributeKeys.RPC_SERVER).get();
        String rpcGroup = channel.attr(AttributeKeys.RPC_GROUP).get();
        NodePoolCache.removeActionRpcSrv(rpcGroup, rpcServer, rpcPoolIndex);
        logger.info("清除rpcPoolIndex={}", rpcPoolIndex);
        channel.close().sync();
    }


}
