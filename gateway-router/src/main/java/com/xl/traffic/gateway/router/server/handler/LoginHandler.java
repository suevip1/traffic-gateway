package com.xl.traffic.gateway.router.server.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.dto.RouterDTO;
import com.xl.traffic.gateway.core.serialize.ISerialize;
import com.xl.traffic.gateway.core.serialize.Protostuff;
import com.xl.traffic.gateway.core.thread.ThreadPoolExecutorUtil;
import com.xl.traffic.gateway.router.service.RouterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.channels.Channel;

/**
 * gateway鉴权,认证
 *
 * @author: xl
 * @date: 2021/7/5
 **/
@Component
@Slf4j
public class LoginHandler implements RouterServerHandlerService {

    private ISerialize iSerialize = new Protostuff();

    @Override
    public void execute(RpcMsg rpcMsg, Channel channel) {
        RouterDTO routerDTO = iSerialize.deserialize(rpcMsg.getBody(), RouterDTO.class);
        String uid = routerDTO.getUid();
        String gatewayIp = routerDTO.getGatewayIp();
        String deviceId = routerDTO.getDeviceId();
        ThreadPoolExecutorUtil.getCommonIOPool().submit(() -> {
            RouterService.getInstance().userLogin(uid, deviceId, gatewayIp);
            log.info("uid:{}->deviceId:{}-> login gatewayIp:{} success!!", uid, deviceId, gatewayIp);
        });
    }
}
