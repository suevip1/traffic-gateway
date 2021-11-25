package com.xl.traffic.gateway.server.rpc.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.cache.CaffineCacheUtil;
import com.xl.traffic.gateway.core.enums.SerializeType;
import com.xl.traffic.gateway.core.serialize.ISerialize;
import com.xl.traffic.gateway.core.serialize.SerializeFactory;
import io.netty.channel.Channel;
import org.springframework.stereotype.Component;


/**
 * 黑名单设置
 *
 * @author: xl
 * @date: 2021/7/5
 **/
@Component
public class BlackIpHandler implements GatewayRpcServerHandlerService {

    ISerialize iSerialize = SerializeFactory.getInstance().getISerialize(SerializeType.protobuf);

    @Override
    public void execute(RpcMsg rpcMsg, Channel channel) {
        String blackIp = new String(rpcMsg.getBody());
        CaffineCacheUtil.getBlackIpCacheMap().put(blackIp, blackIp);
    }

}
