package com.xl.traffic.gateway.router.server.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.dto.RouterDTO;
import com.xl.traffic.gateway.core.enums.SerializeType;
import com.xl.traffic.gateway.core.serialize.ISerialize;
import com.xl.traffic.gateway.core.serialize.SerializeFactory;
import com.xl.traffic.gateway.core.thread.ThreadPoolExecutorUtil;
import com.xl.traffic.gateway.router.service.RouterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * gateway鉴权,认证
 *
 * @author: xl
 * @date: 2021/7/5
 **/
@Component
@Slf4j
public class LoginHandler implements RouterServerHandlerService {

    ISerialize iSerialize = SerializeFactory.getInstance().getISerialize(SerializeType.protobuf);

    @Override
    public void execute(RpcMsg rpcMsg, Connection connection) {
        RouterDTO routerDTO = iSerialize.deserialize(rpcMsg.getBody(), RouterDTO.class);
        String uid = routerDTO.getUid();
        String gatewayIp = routerDTO.getGatewayIp();
        String deviceId = routerDTO.getDeviceId();
        ThreadPoolExecutorUtil.getCommonIOPool().submit(() -> {
            //todo 后期需实现缓存一致性协议
            //todo 现在的流程是 存储在caffine本地+redis，当key不存在时，通过caffine的load机制从redis刷新出来，这样做性能没有利用最大化
            //todo 后期需要改成缓存一致性协议，基于redis pub/sub机制 来同步到其它router集群中最新的用户登录的缓存信息
            RouterService.getInstance().userLogin(uid, deviceId, gatewayIp);
            log.info("save to router,uid:{}->deviceId:{}-> login gatewayIp:{} success!!", uid, deviceId, gatewayIp);
        });
    }
}
