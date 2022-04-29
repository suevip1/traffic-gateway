package com.xl.traffic.gateway.router.server.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.dto.RouterDTO;
import com.xl.traffic.gateway.core.enums.SerializeType;
import com.xl.traffic.gateway.core.serialize.ISerialize;
import com.xl.traffic.gateway.core.serialize.SerializeFactory;
import com.xl.traffic.gateway.core.thread.ThreadPoolExecutorUtil;
import com.xl.traffic.gateway.router.service.RouterService;
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
public class LoginOutHandler implements RouterServerHandlerService {

    ISerialize iSerialize = SerializeFactory.getInstance().getISerialize(SerializeType.protobuf);

    @Override
    public void execute(RpcMsg rpcMsg, Channel channel) {
        RouterDTO routerDTO = iSerialize.deserialize(rpcMsg.getBody(), RouterDTO.class);
        String uid = routerDTO.getUid();
        String gatewayIp = routerDTO.getGatewayIp();
        String deviceId = routerDTO.getDeviceId();
        ThreadPoolExecutorUtil.getRouter_Login_Out_Pool().submit(() -> {
            RouterService.getInstance().exitLogin(uid, deviceId);
            log.info("uid:{}->deviceId:{}-> loginout gatewayIp:{} success!!", uid, deviceId, gatewayIp);
        });
    }
}
