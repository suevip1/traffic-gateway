package com.xl.traffic.gateway.core.server.connection;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 连接管理
 *
 * @author: xl
 * @date: 2021/7/29
 **/
@Slf4j
public class Connection {


    /**
     * 通道id
     */
    @Getter
    private String channelId;

    /**
     * 通道
     */
    private final Channel channel;

    /**
     * 设备id
     */
    @Setter
    @Getter
    private String deviceChannelId;

    /**
     * 源设备id
     */
    @Setter
    @Getter
    private String sourceDeviceId;

    /**
     * 用户id
     */
    @Setter
    @Getter
    private String userId;


    /**
     * 用来做加密解密key
     */
    @Getter
    private String sessionKey;


    public Connection(Channel channel) {
        this.channel = channel;
        channelId = channel.id().asLongText();
    }

    /**
     * 设置连接加解密
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/7/29
     **/
    public void setSessionKey() {
        this.sessionKey = sourceDeviceId + channelId + channelId;
    }

    /**
     * 设置设备通道id，表示唯一标识
     *
     * @param
     * @return: void
     * @author: xl
     * @date: 2021/7/29
     **/
    public void setDeviceChannelId() {
        this.deviceChannelId = sourceDeviceId + GatewayConstants.SEQ + channelId;
    }

    /**
     * 异步发送
     *
     * @param rpcMsg
     * @return: void
     * @author: xl
     * @date: 2021/7/29
     **/
    public void sendMsg(RpcMsg rpcMsg) {
        if (channel.isWritable()) {
            channel.writeAndFlush(rpcMsg);
        } else {
            try {
                /**水位线不够时,需要同步发送，进行阻塞等待水位线下*/
                channel.writeAndFlush(rpcMsg).sync();
                log.info("publish  rpcMsg sended. remoteAddress:[{}], packet:[{}]", channel.remoteAddress(), rpcMsg);
            } catch (InterruptedException e) {
                log.info("write and flush msg exception. packet:[{}]", rpcMsg, e);
            }
        }
    }

}
