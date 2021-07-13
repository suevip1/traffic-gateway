package com.xl.traffic.gateway.server.tcp.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.dto.LoginDTO;
import com.xl.traffic.gateway.core.dto.RouterDTO;
import com.xl.traffic.gateway.core.dto.ack.LoginAck;
import com.xl.traffic.gateway.core.enums.MsgACKType;
import com.xl.traffic.gateway.core.enums.MsgAppNameType;
import com.xl.traffic.gateway.core.enums.MsgGroupType;
import com.xl.traffic.gateway.core.enums.SerializeType;
import com.xl.traffic.gateway.core.model.AuthModel;
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
import org.apache.zookeeper.Login;
import org.springframework.stereotype.Component;


/**
 * gateway的登录
 *
 * @author: xl
 * @date: 2021/7/5
 **/
@Component
@Slf4j
public class LoginHandler implements GatewayServerHandlerService {

    ISerialize serialize = SerializeFactory.getInstance().getISerialize(SerializeType.protobuf);

    @Override
    public void execute(RpcMsg rpcMsg, Channel channel) {
        ThreadPoolExecutorUtil.getCommonIOPool().submit(() -> {
            LoginDTO loginDTO = serialize.deserialize(rpcMsg.getBody(), LoginDTO.class);
            String userId = loginDTO.getUid();
            String deviceId = loginDTO.getDeviceId();
            RouterDTO routerDTO = new RouterDTO(userId, AddressUtils.getInnetIp(), deviceId);
            rpcMsg.setBody(serialize.serialize(routerDTO));
            /**存储用户ip关系*/
            NodePoolManager.getInstance().chooseRpcClient(GatewayConstants.ROUTER_GROUP).sendAsync(rpcMsg);
            /**保存登录关系*/
            ChannelManager.getInstance().addChannel(deviceId, channel);
            /**绑定用户*/
            bindUser(channel, loginDTO);
            /**回执ack*/
            ackLogin(loginDTO, rpcMsg, channel);
        });
    }


    /**
     * ACK回执
     *
     * @param loginDTO 登录实体
     * @param rpcMsg   消息
     * @param channel  通道
     * @return: void
     * @author: xl
     * @date: 2021/7/13
     **/
    public void ackLogin(LoginDTO loginDTO, RpcMsg rpcMsg, Channel channel) {
        /**生成token*/
        String token = Token.createToken(loginDTO);
        LoginAck loginAck = LoginAck.builder()
                .deviceId(loginDTO.getDeviceId())
                .sessionKey(channel.attr(AttributeKeys.SESSION_KEY).get())
                .serverTime(System.currentTimeMillis())
                .uid(loginDTO.getUid())
                .token(token)
                .build();
        rpcMsg.setBody(serialize.serialize(loginAck));
        rpcMsg.setToken(serialize.serialize(token));
        sendMsg(channel, rpcMsg);
    }

    /**
     * 绑定用户
     *
     * @param channel
     * @return: void
     * @author: xl
     * @date: 2021/7/13
     **/
    public void bindUser(Channel channel, LoginDTO loginDTO) {
        String channelId = channel.id().toString();
        String sessionKey = channelId + channelId + loginDTO.getDeviceId();
        //保存设备+ChannelId，区分唯一连接
        String deviceChannelId = loginDTO.getDeviceId() + GatewayConstants.SEQ + channelId;
        channel.attr(AttributeKeys.DEVICE_ID).set(deviceChannelId);
        //原deviceId 不带channelId
        channel.attr(AttributeKeys.SOURCE_DEVICE_ID).set(loginDTO.getDeviceId());
        channel.attr(AttributeKeys.USER_ID).set(loginDTO.getUid());
        channel.attr(AttributeKeys.SESSION_KEY).set(sessionKey);
        log.info("Gateway Bind data userId={}, sourceDeviceId={}, deviceChannelId={}, channelId={}", loginDTO.getUid(), loginDTO.getDeviceId(), deviceChannelId, channelId);
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