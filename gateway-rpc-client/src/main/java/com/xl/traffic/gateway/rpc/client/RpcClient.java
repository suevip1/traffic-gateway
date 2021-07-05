package com.xl.traffic.gateway.rpc.client;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.rpc.callback.CallFuture;
import com.xl.traffic.gateway.rpc.callback.Callback;
import com.xl.traffic.gateway.rpc.callback.CallbackPool;
import com.xl.traffic.gateway.core.exception.RPCException;
import com.xl.traffic.gateway.core.utils.AttributeKeys;
import com.xl.traffic.gateway.rpc.starter.RpcClientStarter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcClient {

    /**
     * 连接索引
     */
    private int index;
    /**
     * 通道
     */
    private Channel channel;
    /**
     * 节点信息
     */
    private ServerNodeInfo nodeInfo;

    private String key;

    public RpcClient(ServerNodeInfo nodeInfo, int index, String key) {
        this.index = index;
        this.nodeInfo = nodeInfo;
        this.key = key;
    }


    public boolean connection() {
        if (isConnect()) {
            log.info("###### channel is open！");
            return true;
        }
        ChannelFuture channelFuture = RpcClientStarter.getInstance().connect(nodeInfo);

        channel = channelFuture.channel();

        channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {

            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                /**channel上绑定rpc数据*/
                channel.attr(AttributeKeys.RPC_SERVER).setIfAbsent(nodeInfo.getIp());
                channel.attr(AttributeKeys.RPC_PORT).setIfAbsent(nodeInfo.getPort());
                channel.attr(AttributeKeys.RPC_INDEX).setIfAbsent(index);
                channel.attr(AttributeKeys.RPC_POOL_KEY).setIfAbsent(key);
                channel.attr(AttributeKeys.RPC_GROUP).setIfAbsent(nodeInfo.getGroup());
                log.info("###### index : {} RPC_SERVER: {} RPC_PORT: {} RPC_POOL_KEY: {} RPC_GROUP:{}",
                        channel.attr(AttributeKeys.RPC_INDEX).get(),
                        channel.attr(AttributeKeys.RPC_SERVER).get(),
                        channel.attr(AttributeKeys.RPC_PORT).get(),
                        channel.attr(AttributeKeys.RPC_POOL_KEY).get(),
                        channel.attr(AttributeKeys.RPC_GROUP).get()
                );
            }
        });
        return isConnect();
    }

    public boolean isConnect() {
        return (channel != null && channel.isOpen() && channel.isActive());
    }

    /**
     * 异步发送,nio ，带回调的
     *
     * @param request  请求参数
     * @param callback 异步回调
     * @param timeout  CallbackPool上下文必须有超时remove机制,否则内存泄漏
     */
    public void sendAsync(RpcMsg request, Callback<RpcMsg> callback, int timeout) {
        /**校验是否已连接*/
        if (isConnect()) {
            if (timeout <= 0) {
                //判断大于0,CallbackPool上下文必须有超时remove机制,否则内存泄漏
                callback.handleError(new RPCException(getClass().getName() +
                        ".sendAsync() timeout must >0 :" + timeout));
                return;
            }
            /**放入回调池中，并设置超时时间，超过时间后，自动清理回调*/
            CallbackPool.put(request.getReqId(), callback, timeout);
            channel.writeAndFlush(request);
        } else {
            callback.handleError(new RPCException(this.getClass().getName()
                    + "-can no connect:" + getInfo()));
        }
    }

    /**
     * 异步发送,nio，不带回调的
     *
     * @param request  请求参数
     */
    public void sendAsync(RpcMsg request) {
        /**校验是否已连接*/
        if (isConnect()) {
            channel.writeAndFlush(request);
        }
    }


    /**
     * 同步,返回响应信息 路由不建议用,访问延迟大将会导致线程挂起太久,CPU无法跑满,而解决方法只有新建更多线程,性能不好
     */
    public RpcMsg sendSync(RpcMsg request, int timeout) throws InterruptedException, RPCException {
        if (isConnect()) {
            CallFuture<RpcMsg> future = CallFuture.newInstance();
            CallbackPool.put(request.getReqId(), future);
            channel.writeAndFlush(request);
            try {
                return future.get(timeout, TimeUnit.MILLISECONDS);
            } finally {
                CallbackPool.remove(request.getReqId());//移除上下文
            }
        } else {
            throw new RPCException(getClass().getName() + ".sendSync() can no connect:" + getInfo());
        }
    }

    public String getInfo() {
        if (channel != null)
            return channel.toString();
        else
            return getIpPort();
    }

    public String getIpPort() {
        return nodeInfo.getIp() + ":" + nodeInfo.getPort();
    }


}
