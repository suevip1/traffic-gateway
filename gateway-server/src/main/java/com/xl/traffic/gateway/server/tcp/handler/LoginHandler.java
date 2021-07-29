package com.xl.traffic.gateway.server.tcp.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.dto.LoginDTO;
import com.xl.traffic.gateway.core.dto.RouterDTO;
import com.xl.traffic.gateway.core.dto.ack.LoginAck;
import com.xl.traffic.gateway.core.enums.SerializeType;
import com.xl.traffic.gateway.core.serialize.ISerialize;
import com.xl.traffic.gateway.core.serialize.SerializeFactory;
import com.xl.traffic.gateway.core.server.connection.Connection;
import com.xl.traffic.gateway.core.thread.ThreadPoolExecutorUtil;
import com.xl.traffic.gateway.core.token.Token;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.rpc.pool.NodePoolManager;
import lombok.extern.slf4j.Slf4j;
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
    public void execute(RpcMsg rpcMsg, Connection connection) {
        ThreadPoolExecutorUtil.getCommonIOPool().submit(() -> {
            LoginDTO loginDTO = serialize.deserialize(rpcMsg.getBody(), LoginDTO.class);
            String userId = loginDTO.getUid();
            String deviceId = loginDTO.getDeviceId();
            RouterDTO routerDTO = new RouterDTO(userId, AddressUtils.getInnetIp(), deviceId);
            rpcMsg.setBody(serialize.serialize(routerDTO));
            /**存储用户ip关系*/
            NodePoolManager.getInstance().chooseRpcClient(GatewayConstants.ROUTER_GROUP).sendAsync(rpcMsg);
            /**绑定用户*/
            bindUser(connection, loginDTO);
            /**回执ack*/
            ackLogin(loginDTO, rpcMsg, connection);
        });
    }


    /**
     * ACK回执
     *
     * @param loginDTO   登录实体
     * @param rpcMsg     消息
     * @param connection 连接
     * @return: void
     * @author: xl
     * @date: 2021/7/13
     **/
    public void ackLogin(LoginDTO loginDTO, RpcMsg rpcMsg, Connection connection) {
        /**生成token*/
        String token = Token.createToken(loginDTO);
        LoginAck loginAck = LoginAck.builder()
                .deviceId(loginDTO.getDeviceId())
                .sessionKey(connection.getSessionKey())
                .serverTime(System.currentTimeMillis())
                .uid(loginDTO.getUid())
                .token(token)
                .build();
        rpcMsg.setBody(serialize.serialize(loginAck));
        rpcMsg.setToken(serialize.serialize(token));
        connection.sendMsg(rpcMsg);
    }

    /**
     * 绑定用户
     *
     * @param connection
     * @return: void
     * @author: xl
     * @date: 2021/7/13
     **/
    public void bindUser(Connection connection, LoginDTO loginDTO) {
        //保存设备+ChannelId，区分唯一连接
        connection.setSourceDeviceId(loginDTO.getDeviceId());
        connection.setUserId(loginDTO.getUid());
        connection.setSessionKey();
        connection.setDeviceChannelId();
        log.info("Gateway Bind data userId={}, sourceDeviceId={}, deviceChannelId={}, channelId={}",
                loginDTO.getUid(), loginDTO.getDeviceId(), connection.getDeviceChannelId(), connection.getChannelId());
    }
}
