package com.xl.traffic.gateway.server.tcp.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.dto.LoginDTO;
import com.xl.traffic.gateway.core.dto.LoginOutDTO;
import com.xl.traffic.gateway.core.dto.RouterDTO;
import com.xl.traffic.gateway.core.dto.ack.LoginAck;
import com.xl.traffic.gateway.core.dto.ack.LoginOutAck;
import com.xl.traffic.gateway.core.enums.SerializeType;
import com.xl.traffic.gateway.core.serialize.ISerialize;
import com.xl.traffic.gateway.core.serialize.SerializeFactory;
import com.xl.traffic.gateway.core.server.manager.ChannelManager;
import com.xl.traffic.gateway.core.thread.ThreadPoolExecutorUtil;
import com.xl.traffic.gateway.core.token.Token;
import com.xl.traffic.gateway.core.utils.AttributeKeys;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.rpc.pool.NodePoolManager;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * gateway退出登录
 *
 * @author: xl
 * @date: 2021/7/5
 **/
@Component
@Slf4j
public class LoginOutHandler implements GatewayServerHandlerService {

    ISerialize serialize = SerializeFactory.getInstance().getISerialize(SerializeType.protobuf);

    @Override
    public void execute(RpcMsg rpcMsg, Channel channel) {
        ThreadPoolExecutorUtil.getCommonIOPool().submit(() -> {
            LoginOutDTO loginOutDTO = serialize.deserialize(rpcMsg.getBody(), LoginOutDTO.class);
            String userId = loginOutDTO.getUid();
            String deviceId = loginOutDTO.getDeviceId();
            RouterDTO routerDTO = new RouterDTO(userId, AddressUtils.getInnetIp(), deviceId);
            rpcMsg.setBody(serialize.serialize(routerDTO));
            /**删除用户ip关系*/
            NodePoolManager.getInstance().chooseRpcClient(GatewayConstants.ROUTER_GROUP).sendAsync(rpcMsg);
            /**删除登录关系*/
            ChannelManager.getInstance().delChannel(deviceId);
            /**回执ack*/
            ackLoginOut(loginOutDTO, rpcMsg, channel);
        });
    }


    /**
     * ACK回执退出登录
     *
     * @param loginDTO 登录实体
     * @param rpcMsg   消息
     * @param channel  通道
     * @return: void
     * @author: xl
     * @date: 2021/7/13
     **/
    public void ackLoginOut(LoginOutDTO loginDTO, RpcMsg rpcMsg, Channel channel) {
        /**生成token*/
        String token = Token.createToken(loginDTO);
        LoginOutAck loginOutAck = LoginOutAck.builder()
                .deviceId(loginDTO.getDeviceId())
                .sessionKey(channel.attr(AttributeKeys.SESSION_KEY).get())
                .serverTime(System.currentTimeMillis())
                .uid(loginDTO.getUid())
                .token(token)
                .build();
        rpcMsg.setBody(serialize.serialize(loginOutAck));
        rpcMsg.setToken(serialize.serialize(token));
        sendMsg(channel, rpcMsg);
    }


    public void sendMsg(Channel channel, RpcMsg rpcMsg) {
        if (channel.isWritable()) {
            channel.writeAndFlush(rpcMsg);
        } else {
            try {
                channel.writeAndFlush(rpcMsg).sync();
                log.info("publish  rpcMsg sended. remoteAddress:[{}], packet:[{}]", channel.remoteAddress(), rpcMsg);
            } catch (InterruptedException e) {
                log.info("write and flush msg exception. packet:[{}]", rpcMsg, e);
            }
        }
    }

}
